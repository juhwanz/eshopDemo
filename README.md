# 🛒 E-Shop: High-Performance E-Commerce Backend

**대규모 트래픽을 고려한 고성능 이커머스 백엔드 API 서비스**입니다.
단순한 기능 구현을 넘어 **동시성 제어, 조회 성능 최적화, 보안 및 배포 자동화** 등 실무에서 마주할 수 있는 기술적 난제들을 해결하는 데 집중했습니다.

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen) ![MySQL](https://img.shields.io/badge/MySQL-8.0-blue) ![Redis](https://img.shields.io/badge/Redis-Caching-red) ![Docker](https://img.shields.io/badge/Docker-Enabled-blueviolet)

---

## 📅 Development Journey (개발 여정 및 문제 해결)

프로젝트 초기 설정부터 배포, 그리고 리팩토링까지 끊임없이 **"왜?"**라는 질문을 던지며 코드를 개선했습니다.

### Phase 1. Core Logic & Architecture (설계와 구현)
- **객체 지향 설계**: `User` - `Order` - `Product` 간의 연관관계를 JPA로 설계하고, 무분별한 Setter 사용을 지양하여 데이터 변경 지점을 통제했습니다.
- **DTO 패턴 도입**: Entity가 컨트롤러 외부로 노출되어 발생하는 보안 문제와 API 스펙 의존성을 해결하기 위해 `Request/Response DTO`를 철저히 분리했습니다.
- **Stateless 인증**: 확장성을 고려하여 세션 대신 **JWT(JSON Web Token)** 기반의 인증 시스템을 직접 구현했습니다.

### Phase 2. Performance Tuning (성능 최적화)
기능 구현 후, 대량의 트래픽 상황을 가정하여 발생한 이슈를 기술적으로 해결했습니다.

#### 1. 동시성 제어 (Concurrency Control) 🔥
- **문제**: 인기 상품 주문 시 100명의 유저가 동시에 요청을 보내면 `Race Condition`으로 인해 재고가 음수가 되거나 정합성이 깨지는 현상 발생.
- **해결**:
    - `Java synchronized`의 한계(분산 환경 미지원)를 인지.
    - **`Pessimistic Lock (비관적 락)`**을 DB Row 레벨에 적용하여 데이터 정합성을 강력하게 보장.
    - `ExecutorService`와 `CountDownLatch`를 활용한 **동시성 테스트 코드(OrderConcurrencyTest)** 작성으로 검증 완료.

#### 2. 조회 성능 개선 (N+1 & Caching) 🚀
- **문제**: 복잡한 상품 검색 시 불필요한 쿼리가 수십 번 발생하는 N+1 문제 확인.
- **해결**:
    - **QueryDSL**을 도입하여 `Fetch Join`으로 연관 데이터를 한 번에 조회.
    - 자주 조회되지만 변경이 적은 데이터(상품 상세)는 **Redis**를 적용(`Look-aside` 패턴)하여 DB 부하를 줄이고 응답 속도를 개선.

### Phase 3. Security & Stability (운영 안정성) 🛡️
- **Secret Management**: DB 비밀번호나 JWT Key 같은 민감 정보가 소스코드에 노출되지 않도록 **환경변수(Environment Variables)** 기반으로 설정을 분리하고, 로컬 개발 시에는 `application-secret.yml`을 활용하도록 구성.
- **Test Coverage**: 단순 단위 테스트를 넘어 `Mockito`를 활용한 서비스 로직 검증 및 통합 테스트 환경 구축.

### Phase 4. DevOps (인프라 및 배포) ☁️
- **Dockerization**: 개발 환경(Mac/M1)과 운영 환경(AWS/Linux)의 아키텍처 차이(`exec format error`)를 극복하기 위해 `Multi-architecture Build` 적용.
- **AWS Deployment**: EC2 인스턴스에 Docker 컨테이너 기반으로 배포하여 환경 일관성 확보.

---

## 🛠 Tech Stack

| Category | Technology | Description |
| --- | --- | --- |
| **Language** | Java 17 | LTS 버전의 안정성 활용 |
| **Framework** | Spring Boot 3.x | 생산성 및 생태계 활용 |
| **Data Access** | JPA, QueryDSL | ORM 표준 및 타입 안정성이 보장된 동적 쿼리 |
| **Database** | MySQL 8.0 | 트랜잭션 처리를 위한 메인 RDBMS |
| **Cache** | Redis | 조회 성능 가속을 위한 인메모리 저장소 |
| **Security** | Spring Security, JWT | Stateless 인증 및 인가 처리 |
| **DevOps** | AWS EC2, Docker | 클라우드 배포 및 컨테이너 환경 |

---

## 🚀 How to Run

### 1. Prerequisites (환경 설정)
이 프로젝트는 **보안을 위해 민감한 설정 정보를 환경변수로 관리**합니다. 실행 전 아래 환경변수 설정이 필요합니다.

```bash
# 환경변수 예시
export DB_PASSWORD=your_db_password
export JWT_SECRET_KEY=your_very_long_secret_key_base64_encoded
2. Run with Docker (Recommended)
MySQL, Redis, Application을 한 번에 실행합니다.

# 1. Network 생성 (없을 경우)
docker network create eshop-net

# 2. MySQL & Redis 실행
docker run -d --name mysql-server --network host -e MYSQL_ROOT_PASSWORD=1234 mysql:8.0
docker run -d --name redis --network host redis

# 3. App Build & Run
./gradlew clean build -x test
docker build -t eshop-app .
docker run -d --name eshop-server --network host \
  -e DB_PASSWORD=1234 \
  -e JWT_SECRET_KEY=V293LFRoaXMtaXMtYS1yZWFsbHktbG9uZy1zZWNyZXQta2V5LWZvci1qd3Qtc2VjdXJpdHkhISEK \
  eshop-app

3. API Documentation
서버 실행 후 아래 주소에서 API 명세를 확인할 수 있습니다.

Swagger UI: http://localhost:8080/swagger-ui/index.html

"돌아가는 코드"에서 "안전하고 효율적인 코드"로. 초기에는 기능 구현에 급급하여 Entity를 그대로 반환하거나 비밀번호를 하드코딩하는 실수가 있었습니다. 하지만 리팩토링 과정을 통해 DTO 패턴으로 API 설계를 바로잡고, 환경변수를 통해 보안을 강화했습니다. 특히 100명의 동시 접속 상황을 테스트 코드로 재현하고 비관적 락으로 해결했을 때의 짜릿함은 백엔드 엔지니어로서의 확신을 주었습니다. 앞으로는 CI/CD 파이프라인(Github Actions) 구축과 대용량 트래픽 처리를 위한 Kafka 도입을 목표로 하고 있습니다.
