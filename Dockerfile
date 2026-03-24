# Dockerfile
FROM openjdk:11-jre
COPY target/university-sms-system-1.0-SNAPSHOT.jar university-sms-system.jar
ENTRYPOINT ["java", "-jar", "university-sms-system.jar"]
