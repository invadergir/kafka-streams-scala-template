FROM localhost/openjdk-11:latest

WORKDIR /opt/app
ENV JARFILE=kafka-streams-scala-template.jar

ADD target/scala-2.13/${JARFILE} .
ADD container/* .
RUN chmod 550 *.sh && chown nonroot. *

USER nonroot
ENTRYPOINT ["./run.sh"]
CMD []
