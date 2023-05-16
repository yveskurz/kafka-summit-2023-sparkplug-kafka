#!/bin/bash

if [ -f "env" ]; then
  echo "Reading in environment variables from from env file."
  export $(grep -v '^#' env | xargs)
fi

docker rm --force kafka-connect-iot

docker run -d \
  --name=kafka-connect-iot \
  -p 8083:8083 \
  -e CONNECT_BOOTSTRAP_SERVERS=$KAFKA_BROKER \
  -e CONNECT_REST_PORT=28082 \
  -e CONNECT_GROUP_ID="iot-connect" \
  -e CONNECT_KEY_CONVERTER="org.apache.kafka.connect.storage.StringConverter" \
  -e CONNECT_VALUE_CONVERTER="org.apache.kafka.connect.converters.ByteArrayConverter" \
  -e CONNECT_CONFIG_STORAGE_TOPIC="iot-connect-config" \
  -e CONNECT_OFFSET_STORAGE_TOPIC="iot-connect-offsets" \
  -e CONNECT_STATUS_STORAGE_TOPIC="iot-connect-status" \
  -e CONNECT_REST_ADVERTISED_HOST_NAME=$KAFKA_CONNECT_REST_ADVERTISED_HOST_NAME \
  -e CONNECT_PLUGIN_PATH="/usr/share/java,/usr/share/confluent-hub-components" \
  -e CONNECT_LOG4J_LOGGERS="de.paul.kafka.connect=DEBUG,org.reflections=ERROR" \
   kafka-summit-2023/iot-connect:0.6.2
