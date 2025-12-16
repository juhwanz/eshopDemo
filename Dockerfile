# ❌ (삭제) 기존 코드
# FROM openjdk:17-jdk-slim

# ⭕️ (변경) 아마존 코레토(Amazon Corretto) 17버전 사용
FROM amazoncorretto:17
# 2. 내 컴퓨터에 있는 jar 파일을 컨테이너 안으로 복사 (짐 옮기기)
# (build/libs 안에 생긴 .jar 파일을 app.jar라는 이름으로 복사함)
COPY build/libs/*SNAPSHOT.jar app.jar

# 3. 컨테이너가 실행될 때 명령어 (서버 켜기)
ENTRYPOINT ["java", "-jar", "app.jar"]