package hello.exception.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ExceptionResolver 구현
 */
@Slf4j
public class MyHandlerExceptionResolver implements HandlerExceptionResolver {

    /**
     * @param request
     * @param response
     * @param handler  핸들러(컨트롤러) 정보
     * @param ex       핸들러(컨트롤러)에서 발생한 발생한 예외
     * @return ModelAndView를 반환하는 이유는 Exception을 처리해서 마치 정상 흐름처럼 변경하는 것이 ExceptionResolver의 목적이기 때문이다.
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            //발생한 예외가 IllegalArgumentException 타입인 경우
            if (ex instanceof IllegalArgumentException) {
                log.info("IllegalArgumentException resolver to 400");

                response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage()); //400 코드 반환

                //빈 ModelAndView를 반환하면 뷰를 렌더링하지 않고 정상 흐름으로 서블릿이 반환된다.
                //ModelAndView를 지정하면, 해당 뷰를 렌더링한다.
                return new ModelAndView();
            }
        } catch (Exception e) {
            log.error("resolver ex", e);
        }

        //null을 반환하면 다음 ExceptionResolver를 찾아서 실행한다.
        //만약, 처리할 수 있는 ExceptionResolver가 없으면 예외 처리가 안되고, 기존에 발생한 예외를 서블릿 밖으로 던진다.
        return null;
    }
}
