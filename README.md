# 🛍️ E-Shop : 고성능 커머스 서비스 백엔드 API

> **대용량 트래픽을 고려한 동시성 제어와 성능 최적화에 초점을 맞춘 이커머스 핵심 API 서버**

---

## 1. 🛠️ 기술 스택 (Tech Stack)

| 구분 | 기술 | 사용 목적 |
| :--- | :--- | :--- |
| **Language** | Java 17 | 최신 LTS 버전 활용 |
| **Framework** | Spring Boot 3.x | 생산성 높은 REST API 서버 구축 |
| **Database** | MySQL, Redis, H2 | 메인 DB, 캐시/세션 저장소, 테스트 DB |
| **ORM** | JPA (Hibernate), QueryDSL | 객체 중심 데이터 설계 및 동적 쿼리 처리 |
| **Security** | Spring Security, JWT | Stateless한 인증/인가 구현 |
| **Test** | JUnit5, Mockito | 단위 테스트 및 동시성 통합 테스트 |

---

## 2. 🔥 핵심 기술적 챌린지 & 해결 (Key Points)

### ① 선착순 주문 시 재고 정합성 보장 (Concurrency Control)
* **문제 상황:** 재고가 1개 남았을 때 다수의 사용자가 동시에 주문을 요청하면, Race Condition으로 인해 재고가 마이너스가 되거나 초과 주문이 접수되는 현상 발생.
* **해결 방법:** DB 레벨의 **`Pessimistic Lock`(비관적 락)**을 적용하여, 트랜잭션이 끝날 때까지 해당 상품의 행(Row)을 잠금 처리.
* **결과:** `OrderConcurrencyTest`를 통해 100명의 동시 요청 시 재고가 정확히 0이 됨을 검증 완료.

### ② 주문 내역 조회 성능 최적화 (N+1 Problem)
* **문제 상황:** 사용자의 주문 목록 조회(`getOrders`) 시, 각 주문(`Order`)마다 연관된 상품(`Product`) 정보를 가져오기 위해 추가적인 SELECT 쿼리가 반복 실행되는 N+1 문제 발생.
* **해결 방법:** JPA의 **`Fetch Join`**을 사용하여 주문, 주문상품, 상품 엔티티를 한 번의 쿼리로 즉시 로딩(Eager Loading)하도록 튜닝하였으나, 페이징 처리 시 OOM유발 위험성 때문에, **`default_batch_fetch_size`**를 설정(100)하여 연관된 엔티티를 **`IN` 쿼리**로 모아서 조회하도록 최적화함.
* **결과:** 쿼리 실행 횟수를 N+1회에서 **단 1회**로 획기적으로 단축.

### ③ 복잡한 상품 검색 조건 처리 (Dynamic Query)
* **문제 상황:** 상품명, 최소/최대 가격 등 다양한 검색 조건이 조합될 수 있어, 모든 경우의 수를 정적 쿼리 메서드로 만들기 불가능.
* **해결 방법:** **`QueryDSL`**을 도입하여 자바 코드로 동적 쿼리를 작성. `BooleanExpression`을 활용해 조건이 `null`일 경우 자동으로 쿼리에서 제외되도록 유연한 검색 기능 구현.
* **결과:** 컴파일 시점에 문법 오류를 잡을 수 있는 Type-Safe한 검색 로직 완성.

### ④ 도메인 주도 설계 (DDD) 적용
* **설계 방식:** 비즈니스 로직(재고 감소, 가격 변경 등)을 서비스 계층이 아닌 **엔티티(`Product`) 내부**에 응집시킴.
* **이점:** 객체 스스로 상태를 관리하게 하여 객체지향적인 설계를 유지하고, 서비스 계층의 코드를 간결하게 유지.

---

## 3. 📂 주요 기능 명세 (Features)

### 👤 회원 시스템 (User)
* **JWT 인증:** Access Token & Refresh Token 이중 토큰 발급 (Redis 연동)
* **권한 관리:** Spring Security Filter를 통한 권한 제어 (일반 유저 / 관리자)

### 📦 상품 시스템 (Product)
* **상품 등록:** 관리자(ADMIN) 권한으로 상품 등록 및 재고 관리
* **상품 검색:** QueryDSL 기반의 다중 조건 검색 (상품명, 가격 범위)
* **캐싱:** Redis 캐싱을 적용하여 조회 성능 개선 (Global Cache Strategy)

### 🧾 주문 시스템 (Order)
* **주문 처리:** 트랜잭션 기반의 주문 생성 및 취소
* **재고 관리:** 주문 생성 시 재고 차감 및 유효성 검증 자동화
* **이력 조회:** 사용자별 주문 이력 조회 (Batch size 최적화)

---

## 4. 🧪 테스트 전략 (Testing)

* **Unit Test:** Mockito를 활용하여 Repository, Security 의존성을 격리시킨 상태에서 Service 계층의 비즈니스 로직 단위 테스트 수행.
* **Integration Test:** H2 데이터베이스 및 멀티 스레드(`ExecutorService`) 환경을 구축하여 실제 동시성 제어 동작 검증.

---

## 5. 🚀 실행 방법 (How to Run)

1.  **사전 요구사항:** Docker (Redis), Java 17
2.  **Redis 실행:**
    ```bash
    docker run -p 6379:6379 redis
    ```
3.  **애플리케이션 실행:**
    ```bash
    ./gradlew bootRun
    ```