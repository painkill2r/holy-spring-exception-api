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

### ExceptionResolver에서 예외를 마무리 하기

1. 기존에는 예외가 발생하면 WAS까지 예외가 던져지고, WAS에서 오류 페이지 정를 찾아서 다시 `/error`를 호출하였다.
    - 생각해보면 과정이 너무 복잡하다.
2. ExceptionResolver을 활용하면 복잡한 과정 없이 예외를 깔끔하게 해결할 수 있다.
    - 컨트롤러에서 예외가 발생해도 ExceptionResolver에서 예외를 처리해 버린다.
    - 따라서 예외가 발생해도 서블릿 컨테이너까지 예외가 전달되지 않고, 스프링 MVC에서 예외 처리는 끝이 난다.
    - 결과적으로 WAS 입장에서는 정상 처리가 된 것이다.
    - `이렇게 예외를 ExceptionResolver에서 처리할 수 있다는 것이 핵심이다.`

## 스프링이 제공하는 ExceptionResolver

1. 스프링 부트가 기본으로 제공하는 ExceptionResolver는 다음과 같다. `HandlerExceptionResolverComposite`에 다음 순서데로 등록된다.
    - ExceptionHandlerExceptionResolver
    - ResponseStatusExceptionResolver
    - DefaultHandlerExceptionResolver(우선 순위가 가장 낮음)

### ExceptionHandlerExceptionResolver

1. `@ExceptionHandler`를 처리한다.
    - API 예외 처리는 대부분 이 기능으로 해결한다.
2. `@ExceptionHandler` 애노테이션을 선언하고, 해당 컨트롤러에서 처리하고 싶은 예외를 지정해주면 된다.
    - 해당 컨트롤러에서 예외가 발생하면 `@ExceptionHandler`이 설정된 메서드가 호출된다.
        - 참고로 지정한 예외 또는 그 예외의 자식 클래스는 모두 잡을 수 있다.
3. 우선 순위
    - 스프링의 우선순위는 항상 자세한 것이 우선권을 가진다. 예를 들어서 부모, 자식 클래스가 있고 다음과 같이 예외가 처리된다.
       ```java
       @ExceptionHandler(부모예외.class) 
       public String 부모예외처리()(부모예외 e) {
       }
       
       @ExceptionHandler(자식예외.class) 
       public String 자식예외처리()(자식예외 e) {
       }
      ```
    - @ExceptionHandler에 지정한 부모 클래스는 자식 클래스까지 처리할 수 있다.
    - 따라서 `자식 예외`가 발생하면 `부모예외처리()`, `자식예외처리()` 둘 다 호출 대상이 된다.
    - 그런데 둘 중 더 자세한 것이 우선권을 가지므로 `자식예외처리()`가 호출된다.
3. 다음과 같이 다양한 예외를 처리할 수도 있다.
   ```java
   @ExceptionHandler({AException.class, BException.class}) 
   public String ex(Exception e) { 
        log.info("exception e", e); 
   }
   ```

#### 실행 흐름

1. (가정)컨트롤러 호출 결과 `IllegalArgumentException` 예외가 컨트롤러 밖으로 던져진다.
2. 예외가 발생했으므로 `ExceptionResolver`가 작동한다.
    - 가장 우선순위가 높은 `ExceptionHandlerExceptionResolver`가 실행된다.
3. `ExceptionHandlerExceptionResolver`는 해당 컨트롤러에 IllegalArguementException을 처리할 수 있는 `@ExceptionHandler`가 있는지 확인한다.
4. 만약에 존재한다면, 해당 @ExceptionHandler(메소드)를 실행하고, 컨트롤러가 @RestController인 경우 HTTP 컨버터가 사용되어 응답이 JSON으로 반환된다.
5. 또한, @ResponseStatus를 설정한 경우 HTTP 상태 코드도 같이 응답한다.
    - `@ResponseStatus(HttpStatus.BAD_REQUEST)`

### ResponseStatusExceptionResolver

1. 예외에 따라서 HTTP 상태 코드와 오류 메시지를를 지정해 주는 역할을 한다.
    - `@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "잘못된 요청 오류")`
        - reason을 `MessageSource`에서 찾는 기능도 제공한다.(reason = "error.bad")
         ```properties
         #messages.properties
         error.bad=잘못된 요청 오류입니다. 메시지 사용
         ```
2. 다음 두가지 경우를 처리한다.
    - @ResponseStatus가 달려 있는 예외
    - ResponseStatusException 예외
        - @ResponseStatus는 개발자가 직접 변경할 수 없는 예외에는 적용할 수 없다.(라이브러리 예외 코드 등)
        - 추가로 애노테이션을 사용하기 때문에 조건에 따라 동적으로 변경하는 것도 어렵다.
        - 이때는 ResponseStatusException 예외를 사용하면 된다.
        - `throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.bad", new IllegalArgumentException());`

### DefaultHandlerExceptionResolver

1. 스프링 내부에서 발생하는 스프링 예외를 해결한다.
    - 대표적으로 파라미터 바인딩 시점에 타입이 맞지 않으면 내부에서 `TypeMismatchException`이 발생하는데
    - 이 경우 예외가 발생했기 때문에 그냥 두면 서블릿 컨테이너까지 오류가 올라가고, 결과적으로 500 오류가 발생한다.
    - 그런데 파라미터 바인딩은 대부분 클라이언트가 HTTP 요청 정보를 잘못 호출해서 발생하는 문제이다.
    - `HTTP 에서는 이런 경우 HTTP 상태 코드 400을 사용하도록 되어 있다.`
    - DefaultHandlerExceptionResolver는 이것을 500 오류가 아니라 HTTP 상태 코드 400 오류로 변경한다.

## @ControllerAdvice, @RestControllerAdvice

1. `@ControllerAdvice`는 대상으로 지정한 여러 컨트롤러에 `@ExceptionHandler`, `@InitBinder` 기능을 부여해주는 역할을 한다.
2. `@ControllerAdvice` 에 대상을 지정하지 않으면 모든 컨트롤러에 적용된다.(글로벌 적용)
    - 대상 컨트롤러 지정 방법
       ```java
      // Target all Controllers annotated with @RestController(특정 어노테이션이 있는 컨트롤러)
      @ControllerAdvice(annotations = RestController.class) 
      public class ExampleAdvice1 {
      
      }

      // Target all Controllers within specific packages(특정 패키지 하위에 있는 컨트롤러)
      @ControllerAdvice("org.example.controllers") 
      public class ExampleAdvice2 {
      
      }

      // Target all Controllers assignable to specific classes(특정 클래스)
      @ControllerAdvice(assignableTypes = {ControllerInterface.class, AbstractController.class}) 
      public class ExampleAdvice3 {
      
      }
       ```
3. `@RestControllerAdvice`는 @ControllerAdvice와 같고, `@ResponseBody`가 추가되어 있다.
    - @Controller와 @RestController의 차이와 같다.
4. `@ExceptionHandler`와 `@ControllerAdvice`를 조합하면 예외를 깔끔하게 해결할 수 있다.