FROM confluentinc/cp-kafka-connect-base:7.2.0

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-mqtt:1.5.1
RUN confluent-hub install --no-prompt confluentinc/connect-transforms:latest

RUN mkdir /usr/share/java/kafkasummit
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/java/kafkasummit

