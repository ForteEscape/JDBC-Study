package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDBException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 예외 누수 문제 해결
 * 트랜잭션 - 트랜잭션 매니저
 * SQLExceptionTranslator 추가
 * DataSourceUtils.getConnection()
 * DataSourceUtils.releaseConnection() 사용
 * check 예외를 런타임 예외로 변경
 */

@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository{
    private final DataSource dataSource;
    private final SQLExceptionTranslator exTranslator;

    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }

    @Override
    public Member save(Member member){
        String sql = "insert into member(member_id, money) values(?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());

            pstmt.executeUpdate();

            return member;
        } catch (SQLException e) {
            throw exTranslator.translate("save", sql, e);
        } finally{
            close(conn, pstmt, null);
        }
    }

    @Override
    public Member findById(String memberId){
        String sql = "select * from member where member_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;

        try{
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memberId);

            resultSet = pstmt.executeQuery();

            if (resultSet.next()){
                Member member = new Member();
                member.setMemberId(resultSet.getString("member_id"));
                member.setMoney(resultSet.getInt("money"));

                return member;
            } else{
                throw new NoSuchElementException("member not found, member_id : " + memberId);
            }
        } catch (SQLException e){
            throw exTranslator.translate("findById", sql, e);
        } finally {
            close(conn, pstmt, resultSet);
        }
    }

    public Member findById(Connection connection, String memberId){
        String sql = "select * from member where member_id = ?";

        PreparedStatement pstmt = null;
        ResultSet resultSet = null;

        try{
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, memberId);

            resultSet = pstmt.executeQuery();

            if (resultSet.next()){
                Member member = new Member();
                member.setMemberId(resultSet.getString("member_id"));
                member.setMoney(resultSet.getInt("money"));

                return member;
            } else{
                throw new NoSuchElementException("member not found, member_id : " + memberId);
            }
        } catch (SQLException e){
            throw exTranslator.translate("findById", sql, e);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(pstmt);
        }
    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money = ? where member_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try{
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);

            int resultSize = pstmt.executeUpdate();
        } catch(SQLException e){
            throw exTranslator.translate("update", sql, e);
        } finally {
            close(conn, pstmt, null);
        }
    }

    public void update(Connection connection, String memberId, int money) {
        String sql = "update member set money = ? where member_id = ?";

        PreparedStatement pstmt = null;

        try{
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);

            int resultSize = pstmt.executeUpdate();
        } catch(SQLException e){
            throw exTranslator.translate("update", sql, e);
        } finally {
            JdbcUtils.closeStatement(pstmt);
        }
    }

    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try{
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memberId);

            int resultSize = pstmt.executeUpdate();
        } catch(SQLException e){
            throw exTranslator.translate("delete", sql, e);
        } finally {
            close(conn, pstmt, null);
        }
    }

    private void close(Connection conn, Statement stmt, ResultSet rs){
        // close db connection with JdbcUtils
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);

        // 트랜잭션 동기화를 사용하기 위해 DataSourceUtils를 사용
        DataSourceUtils.releaseConnection(conn, dataSource);
    }

    private Connection getConnection() throws SQLException {
//        Connection connection = dataSource.getConnection();

        // 트랜잭션 동기화를 사용하기 위해 DataSourceUtils를 사용해야 한다.
        Connection connection = DataSourceUtils.getConnection(dataSource);
        log.info("get connection={}, class = {}", connection, connection.getClass());

        return connection;
    }
}
