package hello.exception.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 기본적으로 RuntimeException은 500 에러 코드를 반환하지만
 * @ResponseStatus(value = 상태 코드, reason = "오류 메시지")를 사용해서 400 에러 코드를 반환하도록 설정
 * reason 속성 같은 경우 MessageSource에서 찾는 기능도 제공한다.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "error.bad")
public class BadRequestException extends RuntimeException {
}
