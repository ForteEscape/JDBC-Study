package hello.exception.translator;

import hello.jdbc.connection.ConnectionConst;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class SpringExceptionTranslatorTest {

    DataSource dataSource;

    @BeforeEach
    void init(){
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    @Test
    void sqlExceptionErrorCode(){
        String sql = "select bad gramer";

        try{
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeQuery();
        } catch (SQLException e){
            Assertions.assertThat(e.getErrorCode()).isEqualTo(42122);
            log.info("errorCode = {}", e.getErrorCode(), e);
        }
    }

    @Test
    void exceptionTranslator(){
        String sql = "select bad grammer";

        try{
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeQuery();
        } catch (SQLException e){
            Assertions.assertThat(e.getErrorCode()).isEqualTo(42122);

            SQLErrorCodeSQLExceptionTranslator exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
            DataAccessException resultException = exTranslator.translate("select", sql, e);

            log.info("result", resultException);

            Assertions.assertThat(resultException.getClass()).isEqualTo(BadSqlGrammarException.class);
        }
    }
}
