package hello.exception.api;

import hello.exception.exception.UserException;
import hello.exception.exhandler.ErrorResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class ApiExceptionV2Controller {

    /**
     * [IllegalArgumentException 처리]
     * 해당 컨트롤러에서 IllegalArgumentException 예외가 발생하면
     * 이 메소드가 예외를 처리한다. 그리고 그 결과를 JSON으로 반환한다.(@RestController 이기 때문)
     * <p>
     * 내부적으로 ExceptionHandlerExceptionResolver가 @ExceptionHandler가 설정된 메소드를 찾아줌.
     * <p>
     * 기본적으로 ExceptionResolver를 거치면 정상 흐름으로 판단하여 200 상태 코드를 반환한다.
     * 이것을 각 상황에 맞게 상태 코드를 변경하기 위해서는 @ResponseStatus를 사용하면 된다.
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResult illegalExHandler(IllegalArgumentException e) {
        log.error("[exceptionHandler] ex", e);

        return new ErrorResult("BAD", e.getMessage());
    }

    /**
     * [UserException 처리]
     * @ExceptionHandler에 예외를 생략할 수 있다.
     * 생략하면 파라미의 예외가 지정된다.
     *
     * @param e
     * @return
     */
    @ExceptionHandler
    public ResponseEntity<ErrorResult> userExHandler(UserException e) {
        log.error("[exceptionHandler] ex", e);

        ErrorResult errorResult = new ErrorResult("USER-EX", e.getMessage());

        return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST); //응답 Body와 상태 코드 설정
    }

    /**
     * [Exception 처리]
     * illegalExHandler(), userExHandler()에서 처리하지 못한 예외를 처리한다.
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ErrorResult exHandler(Exception e) {
        log.error("[exceptionHandler] ex", e);

        return new ErrorResult("EX", "내부 오류");
    }

    @GetMapping("/api2/members/{id}")
    public MemberDto getMember(@PathVariable("id") String id) {
        //"ex"가 전달이 되면 RuntimeException 발생
        if (id.equals("ex")) {
            throw new RuntimeException("잘못된 사용자");
        }

        //"bad"가 전달이 되면 IllegalArgumentException 발생
        if (id.equals("bad")) {
            throw new IllegalArgumentException("잘못된 입력 값");
        }

        //"user-ex"가 전달이 되면 UserException 발생
        if (id.equals("user-ex")) {
            throw new UserException("사용자 오류");
        }

        return new MemberDto(id, "hello " + id);
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String memberId;
        private String name;
    }
}
