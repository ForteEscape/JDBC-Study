package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {
    private final MemberRepositoryV2 memberRepositoryV2;
    private final DataSource dataSource;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection conn = dataSource.getConnection();
        try{
            // start transaction
            conn.setAutoCommit(false);

            // business logic
            businessLogic(conn, fromId, toId, money);

            // business logic finished successful
            conn.commit();

        } catch (Exception e) {
            conn.rollback();
            throw new RuntimeException(e);
        } finally {
            releaseConnection(conn);
        }
    }

    private void businessLogic(Connection conn, String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepositoryV2.findById(fromId);
        Member toMember = memberRepositoryV2.findById(toId);

        memberRepositoryV2.update(conn, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepositoryV2.update(conn, toId, toMember.getMoney() + money);
    }

    private static void releaseConnection(Connection conn) {
        if (conn != null){
            try{
                conn.setAutoCommit(true); // connection pool 고려
                conn.close();
            }catch (Exception e){
                log.info("error", e);
                throw new RuntimeException(e);
            }
        }
    }

    private static void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }
}
