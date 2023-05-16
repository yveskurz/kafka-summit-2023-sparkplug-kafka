#!/bin/bash

if [ -f "env" ]; then
  echo "Reading in environment variables from from env file."
  export $(grep -v '^#' env | xargs)
fi
 

if ! command -v jq  &> /dev/null
then
  export PRINT="echo"
else 
  export PRINT="jq"
fi

while true; do
    echo "Waiting for Kafka Connect to start listening ⏳"
    curl -s $KAFKA_CONNECT_REST_URI > /dev/null
    if [[ $? -eq 0 ]]; then 
        break
    fi
    sleep 5
done



echo "Delete old MQTT Source connnector"
curl -X DELETE  $KAFKA_CONNECT_REST_URI/connectors/mqtt-src 

echo "Configuring new MQTT Source connector"
curl -X POST \
  $KAFKA_CONNECT_REST_URI/connectors \
  -H 'Content-Type: application/json' \
  -d '{ "name": "mqtt-src",
    "config":
    {
      "connector.class":"MqttSource",

      "kafka.topic": "'"$KAFKA_MQTT_SOURCE_TOPIC"'",
    
      "mqtt.server.uri": "'"$MQTT_BROKER_URI"'",
      "mqtt.topics": "spBv1.0/#",
      "mqtt.clean.session.enabled": "false",
      
      "transforms": "repartition",
      "transforms.repartition.type": "de.paul.kafka.connect.SparkplugEdgeNodeIdPartitioner",
      "transforms.repartition.partitions": "'"$KAFKA_MQTT_SOURCE_TOPIC_PARTITIONS"'",
    
      "tasks.max": "1"
    }
}
' | $PRINT


echo "Delete old MQTT Sink connnector"
curl -X DELETE  $KAFKA_CONNECT_REST_URI/connectors/mqtt-sink 

echo "Configuring new MQTT Sink connector"
curl -X POST \
  $KAFKA_CONNECT_REST_URI/connectors \
  -H 'Content-Type: application/json' \
  -d '{ "name": "mqtt-sink",
    "config":
    {
      "connector.class":"MqttSink",

      "topics": "'"$KAFKA_MQTT_SINK_TOPIC"'",

      "mqtt.server.uri": "'"$MQTT_BROKER_URI"'",
      "mqtt.retained.enabled": "false", 
      
      "value.converter": "org.apache.kafka.connect.converters.ByteArrayConverter",

      "tasks.max": "1"
    }
}
' | $PRINT

