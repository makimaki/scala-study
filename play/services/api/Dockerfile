FROM adoptopenjdk/openjdk8:jre8u222-b10-alpine

RUN apk --no-cache add bash

ADD target/universal/albot-message-api.zip .

RUN unzip albot-message-api.zip

WORKDIR albot-message-api

EXPOSE 8080

CMD bin/message-api -Dhttp.port=8080
