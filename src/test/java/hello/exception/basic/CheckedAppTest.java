package hello.exception.basic;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

public class CheckedAppTest {

    @Test
    void checked(){
        Controller controller = new Controller();

        Assertions.assertThatThrownBy(controller::request).isInstanceOf(Exception.class);
    }

    static class Controller{
        Service service = new Service();

        public void request() throws SQLException, ConnectException {
            service.logic();
        }
    }

    static class Service{
        Repository repository = new Repository();
        NetworkClient client = new NetworkClient();

        public void logic() throws SQLException, ConnectException {
            repository.call();
            client.call();
        }
    }

    static class NetworkClient{
        public void call() throws ConnectException {
            throw new ConnectException("connection fail");
        }
    }

    static class Repository{
        public void call() throws SQLException {
            throw new SQLException("exception");
        }
    }
}
