import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkplugEdgeNodeIdCustomPartitioner implements Partitioner {
    private static final Logger log = LoggerFactory.getLogger(SparkplugEdgeNodeIdCustomPartitioner.class);

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        if (key == null) {
            log.error("Sparkplug topic cannot be NULL, not a valid Sparkplug topic string.");
            return 0;
        }

        String[] spTopicElements = key.toString().split("/");
        if (spTopicElements.length > 3) {
            String eonId = spTopicElements[3];
            byte[] eonIdBytes = eonId.getBytes();

            List<PartitionInfo> partitions = cluster.availablePartitionsForTopic(topic);
            int partition = Utils.toPositive(Utils.murmur2(eonIdBytes)) % partitions.size();

            log.debug("Message for {} partitioned into partition: {}", key, partition);
            return partition;
        } else {
            log.error("Sparkplug topic '{}' not a valid Sparkplug topic string. Ignoring message.", key);
            return 0;
        }
    }

    @Override
    public void configure(Map<String, ?> configs) {

    }

    @Override
    public void close() {

    }
}
