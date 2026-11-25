# ---------- Stage 1: build WAR dengan Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY db ./db
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: runtime Tomcat 10.1 ----------
FROM tomcat:10.1-jdk17

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /build/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
