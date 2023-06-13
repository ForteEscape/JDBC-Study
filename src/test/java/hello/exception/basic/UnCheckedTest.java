package hello.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class UnCheckedTest {

    @Test
    void unchecked_catch(){
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void unchecked_throw(){
        Service service = new Service();
        service.callThrow();
    }

    static class MyUnCheckedException extends RuntimeException{
        public MyUnCheckedException(String msg){
            super(msg);
        }
    }

    static class Service{
        Repository repository = new Repository();

        /**
         * 예외를 잡아서 처리하는 코드
         */
        public void callCatch(){
            try {
                repository.call();
            } catch (MyUnCheckedException e) {
                log.info("예외 처리 message={}", e.getMessage(), e);
            }
        }

        /**
         * 체크 예외를 밖으로 던지는 코드
         * 체크 예외는 예외를 잡지 않고 밖으로 던지려면 throws를 통해 메서드에 명시해야함
         * @throws MyUnCheckedException
         */
        public void callThrow() throws MyUnCheckedException {
            repository.call();
        }
    }

    static class Repository{
        public void call(){
            throw new MyUnCheckedException("exception");
        }
    }
}
