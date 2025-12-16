🛒 E-Shop
High-Performance E-Commerce Backend (Spring Boot)

Spring Boot 기반 이커머스 백엔드 API 프로젝트로,
인증/인가, 트랜잭션, 동시성 제어, 조회 성능 문제를 실제 시나리오와 테스트를 통해 검증하며 해결했습니다.

📌 Project Overview

목표
단순 CRUD 구현을 넘어서, 실무에서 자주 발생하는 정합성·성능·보안 이슈를 직접 재현하고 해결하는 것

핵심 포인트

JWT 기반 Stateless 인증/인가

재고 정합성을 보장하는 동시성 제어

N+1 문제 및 조회 성능 최적화

Redis 캐시 적용

Docker 기반 실행 및 배포 경험

🧰 Tech Stack
Category	Technology
Language	Java 17
Framework	Spring Boot 3.x
ORM	Spring Data JPA (Hibernate)
Query	QueryDSL
Security	Spring Security, JWT
Database	MySQL 8.0
Cache	Redis
Test	JUnit5, Mockito
DevOps	Docker, AWS EC2
🏗 Architecture
Controller (API)
↓ DTO
Service (비즈니스 로직 / 트랜잭션)
↓
Repository (JPA / QueryDSL)
↓
MySQL / Redis


Entity는 외부에 직접 노출하지 않고 DTO로 API 스펙을 분리

비즈니스 규칙은 Service/Domain 계층에 집중

🔐 Authentication & Authorization

JWT 기반 Stateless 인증 적용

Spring Security Filter Chain 구성

JwtAuthenticationFilter에서 토큰 검증

UserDetailsService + Adapter 패턴으로 도메인 User 연동

선택 이유

Session 방식 대비 확장성과 서버 부하 측면에서 유리

인증 상태를 서버에 저장하지 않아 수평 확장에 적합

🧩 Domain Design
Order / OrderItem 구조

Order ↔ Product 간 다대다 관계를 OrderItem으로 분리

주문 시점의 가격과 수량을 OrderItem에 스냅샷으로 저장

효과

주문 내역 변경 방지

데이터 정합성 유지

실무 이커머스 구조와 유사한 모델링

🚀 Performance & Stability
1️⃣ 동시성 제어 (재고 정합성)

문제

동일 상품에 다수의 동시 주문 요청 시 재고 수량 불일치 발생

원인

동시에 같은 row를 읽고 수정하는 Race Condition

해결

DB Row-level Pessimistic Lock 적용

Java synchronized 대신 DB 트랜잭션 기반 락 선택

검증

ExecutorService + CountDownLatch를 활용한
동시성 테스트 코드(OrderConcurrencyTest) 로 재현 및 검증

2️⃣ 조회 성능 개선 (N+1 문제)

문제

주문 및 상품 조회 시 연관 엔티티로 인해 다수의 쿼리 발생

해결

QueryDSL + Fetch Join 적용

필요 데이터만 한 번에 조회

결과

불필요한 추가 쿼리 제거

Hibernate SQL 로그 기준 쿼리 수 감소 확인

3️⃣ Redis Cache 적용

변경 빈도 대비 조회 빈도가 높은 상품 단건 조회에 Redis 캐시 적용

Look-aside 패턴 사용

효과

반복 조회 시 DB 접근 감소

응답 속도 개선

🧪 Test Strategy

Service 단위 테스트

Mockito 기반 의존성 Mock

Given–When–Then 구조

테스트 작성 중 Mock 설정 누락으로 인한 NPE 발생 경험

테스트를 통해 DI 및 객체 생명주기 이해 강화

🚢 Deployment

Docker 기반 애플리케이션 컨테이너화

로컬(Mac) ↔ 운영(Linux) 환경 차이 고려

AWS EC2에서 Docker 이미지 실행

🔐 Configuration & Security

DB 비밀번호, JWT Secret Key 등 민감 정보는 환경변수로 관리

로컬 개발 시 application-secret.yml 사용 (Git 제외)

🧠 What I Learned

JPA 영속성 컨텍스트와 트랜잭션 동작 원리

Spring Security 인증 흐름의 실제 구조

동시성 문제는 코드가 아니라 시나리오와 테스트로 검증해야 함

성능 이슈는 추측이 아니라 로그와 쿼리로 확인

📈 Next Steps

Redis 캐시 무효화 전략 고도화

Kafka 기반 주문 이벤트 처리

GitHub Actions CI/CD 파이프라인 구축

부하 테스트 기반 성능 수치화