#### 최초 작성일 : 2021.07.24(토)

# Spring Boot API 예외 처리

Spring Boot에서 API에 대한 예외 처리 학습

## 학습 환경

1. OS : MacOS
2. JDK : OpenJDK 11.0.5
3. Framework : Spring Boot 2.5.3
    - [Spring Initializer 링크 : https://start.spring.io](https://start.spring.io)
    - 패키징 : jar
    - 의존설정(Dependencies)
        - Spring Web
        - Thymeleaf
        - Lombok
        - Validation
4. Build Tools : Gradle

## API 예외 처리

1. 지금까지 HTML을 사용한 오류 페이지는 단순히 고객에게 오류 화면을 보여주고 끝이 났다.
    - 4xx, 5xx HTTML 오류 페이지
2. 하지만 API는 각 오류 상황에 맞는 `오류 응답 스펙`을 정하고, JSON으로 데이터를 내려주어야 한다.
    - 클라이언트는 정상 요청이든, 오류 요청이든 JSON이 반환되기를 기대한다.

### 스프링 부트 기본 오류 처리

1. 스프링 부트의 기본 설정은 오류 발생시 `/error`를 오류 페이지로 요청하고, `BasicErrorController`는 이 경로를 기본으로 받는다.
    - server.error.path로 수정 가능(기본 값: /error)
2. 스프링 부트는 `BasicErrorController`가 제공하는 기본 정보들을 활용해서 오류 API를 생성해 준다.
    - application.properties 파일에 다음 옵션을 설정하면 더 자세한 오류 정보를 추가할 수 있다.
    - 물론 오류 메시지 출력은 보안상 위험할 수 있으니, 간결한 메시지만 노출하고, 로그를 통해서 확인하는게 좋다.
       ```properties
       server.error.include-exception=true
       server.error.include-binding-errors=always
       server.error.include-message=always
       server.error.include-stacktrace=always
       ```

### 정리

1. 스프링 부트가 제공하는 `BasicErrorController`는 HTML 페이지를 제공하는 경우에는 매우 편리하다.
2. 하지만 API 오류 처리는 각 API 마다 각각의 컨트롤러나 예외마다 서로 다른 응답 결과를 출력해야 할 수도 있다.
    - 예를 들어서 회원과 관련된 API에서 예외가 발생할 때 응답과, 상품과 관련된 API에서 발생하는 예외에 따라 그 결과가 달라질 수 있다.
3. 결과적으로 API 예외 처리는 매우 세밀하고 복잡하다.
4. 따라서 BasicErrorController를 이용하는 방법은 HTML 화면을 처리할 때 사용하고, API의 오류 처리는 뒤에서 설명할 `@ExceptionHandler`를 이용하자.

## HandlerExceptionResolver

1. 스프링 MVC는 컨트롤러(핸들러) 밖으로 예외가 던져진 경우 예외를 해결하고, 동작을 새로 정의할 수 있는 방법을 제공한다.
2. 컨트롤러 밖으로 던져진 예외를 해결하고, 동작 방식을 변경하고 싶으면 `HandlerExceptionResolver`를 사용하면 된다.
    - 줄여서 `ExceptionHandler`라고 한다.
3. 참고로 ExceptionResolver로 예외를 해결해도 `Interceptor의 postHandle()`은 호출되지 않는다.

### ExceptionResolver 활용

1. 예외 상태 코드 변환
    - 예외를 `response.sendError(...)` 호출로 변경한 후 서블릿에서 상태 코드에 따른 오류를 처리하도록 위임
    - 이후 WAS는 서블릿 오류 페이지를 찾아서 내부 호출(예를 들어, 스프링 부트가 기본으로 설정한 `/error`)
2. 뷰 템플릿 처리
    - `ModelAndView`에 값을 채워서 예외에 따른 새로운 오류 화면을 뷰 렌더링 해서 고객에게 제공
3. API 응답 처리
    - `response.getWriter().println("hello);` 처럼 HTTP 응답 Body에 직접 데이터를 넣어주는 것도 가능하다.
    - 여기에 JSON으로 응답하면 API 응답 처리를 할 수 있다.