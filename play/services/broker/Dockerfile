FROM adoptopenjdk/openjdk8:jre8u222-b10-alpine

ADD target/scala-2.11/albot-message-broker.jar .

CMD java $JAVA_OPTS -cp albot-message-broker.jar
