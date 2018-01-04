FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ENV SPRING_CONFIG_NAME="eidasProperties"
COPY eidasverifyservice*.jar app.jar
COPY eidasProperties.yaml eidasProperties.yaml
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
