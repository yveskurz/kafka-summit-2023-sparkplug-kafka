package de.paul.kafka.connect;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.utils.Utils;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.transforms.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public  class SparkplugEdgeNodeIdPartitioner<R extends ConnectRecord<R>> implements Transformation<R> {
  private static final Logger log = LoggerFactory.getLogger(SparkplugEdgeNodeIdPartitioner.class);

  private static final String PARTITIONS_CONFIG = "partitions";
  private static final String PARTITIONS_DOC = "The number of partitions in destination Kafka topic";
  private static final int PARTITIONS_DEFAULT = 1;

  public static final String OVERVIEW_DOC =
    "Extracts the EoN ID of a Sparkplug v1.0 topic string, uses only this ID to partition the message.";


  private static final ConfigDef CONFIG_DEF = new ConfigDef().define(PARTITIONS_CONFIG, Type.INT, PARTITIONS_DEFAULT, Importance.HIGH, PARTITIONS_DOC);
  private int partitions;


 @Override
  public void configure(Map configs) {
    AbstractConfig config = new AbstractConfig(CONFIG_DEF, configs);
    partitions = config.getInt(PARTITIONS_CONFIG);
  }

  @Override
  public R apply(R record) {
    try {
      Object key = record.key();
      log.debug("Got new message, key: {}", key);

      if (key == null) {
          log.error("Sparkplug topic cannot be NULL, not a valid Sparkplug topic string, message cannot be repartitoned and will be ignored");
          return null;
      }

      String topicString = key.toString();
      String[] spTopicElements = topicString.split("/");
    
      if (spTopicElements.length > 3) {
        String eonId = spTopicElements[3];
        byte[] eonIdBytes = eonId.getBytes();
        int partition = Utils.toPositive(Utils.murmur2(eonIdBytes)) % partitions;

        log.debug("Message for {} partitioned into partition: {}", topicString, partition);
        return record.newRecord(record.topic(), partition, record.keySchema(), record.key(), record.valueSchema(), record.value(), record.timestamp(), record.headers());

      } else {
        log.error("Sparkplug topic '{}' not a valid Sparkplug topic string. Ignoring message.", topicString);
        return null;
      }
    
    } catch (Exception e) {
      log.error("UNEXPECTED ERROR, message ignored.", e);
      return null;
    }
  }

  @Override
  public ConfigDef config() {
    return CONFIG_DEF;
  }

  @Override
  public void close() {
  }
}


