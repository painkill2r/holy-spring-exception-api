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

### 스브링 부트 기본 오류 처리

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