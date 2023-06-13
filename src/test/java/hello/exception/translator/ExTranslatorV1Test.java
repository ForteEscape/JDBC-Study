package hello.exception.translator;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDBException;
import hello.jdbc.repository.ex.MyDuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ExTranslatorV1Test {

    Repository repository;
    Service service;

    @BeforeEach
    void init(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        repository = new Repository(dataSource);
        service = new Service(repository);
    }

    @Test
    @DisplayName("중복 키 입력으로 인한 예외")
    void duplicateKeyTest(){
        service.create("userA");
        service.create("userA");
        //Assertions.assertThatThrownBy(() -> service.create("userA")).isInstanceOf(MyDuplicateKeyException.class);
    }

    @Slf4j
    @RequiredArgsConstructor
    static class Service{
        private final Repository repository;

        void create(String memberId){
            try{
                repository.save(new Member(memberId, 0));
                log.info("saveId = {}", memberId);
            } catch (MyDuplicateKeyException e){
                log.info("key duplicate");
                String retryId = generateNewId(memberId);

                log.info("retryId = {}", retryId);
                repository.save(new Member(retryId, 0));
            } catch (MyDBException e){
                log.info("exception occurred", e);
                throw e;
            }
        }

        private String generateNewId(String memberId){
            return memberId + new Random().nextInt(10000);
        }
    }

    @RequiredArgsConstructor
    static class Repository{
        private final DataSource dataSource;

        public Member save(Member member){
            String sql = "insert into member(member_id, money) values(?, ?)";
            Connection conn = null;
            PreparedStatement pstmt = null;

            try{
                conn = dataSource.getConnection();
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, member.getMemberId());
                pstmt.setInt(2, member.getMoney());
                pstmt.executeUpdate();

                return member;
            } catch (SQLException e){
                if (e.getErrorCode() == 23505){
                    throw new MyDuplicateKeyException(e);
                }

                throw new MyDBException(e);
            } finally {
                JdbcUtils.closeStatement(pstmt);
                JdbcUtils.closeConnection(conn);
            }
        }
    }
}