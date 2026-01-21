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

### ① 선착순 주문 시 '장애 전파' 차단 및 가용성 확보
* **문제 상황:** 재고 정합성을 위해 **DB 비관적 락(Pessimistic Lock)**을 적용했으나, 주문 트래픽 폭주 시 **DB 커넥션 풀(HikariCP) 고갈**로 인해 일반 조회 API까지 **가용성이 0%로 떨어지는 '장애 전파(Cascading Failure)'** 현상 발생.
* **해결 방법:** 락의 대기열을 DB가 아닌 Redis 메모리로 격리시키는 **Redis 분산 락(Redisson)** 도입. Pub/Sub 방식을 사용하여 스핀 락(Spin Lock)으로 인한 Redis 부하를 최소화함.
* **결과:** 동일한 50명 동시 주문 테스트 환경에서 **조회 성공률을 0%에서 100%로 개선**하여 시스템 안정성 확보.

### ② 대용량 데이터 조회 성능 12배 최적화 (Deep Pagination)
* **문제 상황:** 상품 데이터가 50만 건 이상일 때, 기존 `OFFSET` 방식의 페이징은 뒤로 갈수록 **Full Scan**이 발생하여 조회 속도가 급격히 저하됨 (첫 페이지 4ms vs 끝 페이지 48ms).
* **해결 방법:** **No-Offset(커서 기반) 페이징**을 도입하여, `OFFSET`을 제거하고 **인덱스(Index)**를 타도록 쿼리 튜닝.
* **결과:** 데이터 위치와 상관없이 **항상 4ms 내외의 속도(약 12배 성능 향상)**를 유지하며 확장성(Scalability) 확보.

### ③ JPA N+1 문제 해결 및 쿼리 다이어트
* **문제 상황:** 주문 목록 조회 시, 각 주문에 연관된 상품 정보를 가져오기 위해 **N+1 문제(10건 조회 시 쿼리 11번 발생)**가 발생. `Fetch Join`은 페이징 시 메모리 이슈(OOM) 위험이 있어 배제.
* **해결 방법:** **`default_batch_fetch_size`**를 설정(100)하여 연관된 엔티티를 **`IN` 절**로 묶어서 한 번에 조회하도록 최적화.
* **결과:** 쿼리 실행 횟수를 11회에서 **2회로 약 90% 감소**시킴.

### ④ Redis 캐시 정합성 보장 (Cache Consistency)
* **문제 상황:** 조회 성능 개선을 위해 **Look-aside 캐싱**을 적용했으나, 상품 가격 수정 시 DB와 캐시 간 데이터 불일치(Inconsistency) 위험 존재.
* **해결 방법:** 데이터 수정 시 캐시를 갱신(Update)하는 대신 **즉시 삭제(Evict)**하는 전략을 채택. 삭제 후 다음 조회 시 필연적으로 DB의 최신 값을 가져오도록 강제함.
* **결과:** 통합 테스트를 통해 수정 직후 조회 시 최신 데이터가 반환됨을 검증, **성능과 정합성을 동시에 확보**.

---

## 3. 📂 주요 기능 명세 (Features)

### 📦 상품 시스템 (Product)
* **조회 최적화:** No-Offset 페이징 적용으로 1억 건 데이터 대비
* **검색:** QueryDSL 기반의 다중 조건 동적 쿼리
* **캐싱:** Redis Global Cache 적용 및 Eviction 전략 수립

### 🧾 주문 시스템 (Order)
* **동시성 제어:** Redis 분산 락을 활용한 재고 차감 (Race Condition 해결)
* **트랜잭션 관리:** Facade 패턴을 적용하여 락과 트랜잭션의 관심사 분리
* **이력 조회:** Batch Size 최적화를 통한 고성능 조회

### 👤 회원 시스템 (User)
* **인증/인가:** JWT 기반의 Stateless 인증 및 Spring Security 권한 제어

---

## 4. 🧪 테스트 전략 (Testing)

## 4. 🧪 테스트 전략 (Test Strategy)

* **Concurrency Test:** `ExecutorService`와 `CountDownLatch`를 활용하여 **100명 동시 요청 시나리오**를 재현, 재고 정합성(0개 도달)과 서비스 가용성을 검증.
* **Integration Test:** H2 및 TestContainer 환경에서 캐시 Eviction, N+1 쿼리 발생 여부 등 **실제 운영 환경과 유사한 시나리오** 검증.


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