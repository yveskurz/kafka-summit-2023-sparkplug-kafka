/*
 * Copyright © 2019 Christopher Matta (chris.matta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.paul.kafka.connect;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.After;
import org.junit.Test;

import junit.framework.Assert;

public class SparkplugEdgeNodeIdPartitionerTest {

  SparkplugEdgeNodeIdPartitioner smt = new SparkplugEdgeNodeIdPartitioner();

  @After
  public void tearDown() throws Exception {
    smt.close();
  }

  @Test
  public void nullKeyShouldReturnNullRecored() {
    final Map<String, Object> props = new HashMap<>();
     smt.configure(props);

     final ConnectRecord in = new SourceRecord(null, null, null, null, null, null, null);

     final ConnectRecord out = smt.apply(in);
     Assert.assertNull(out);
     
  }


  @Test
  public void invalidTopicShouldReturnNullRecored() {
    final Map<String, Object> props = new HashMap<>();
    smt.configure(props);

    String[] invalidTopicNames = new String [] {
      "",
      "spBv1.0",
      "spBv1.0/PAULv0",
      "spBv1.0/PAULv0/DDATA",
    };

    for(String invalidTopicName : invalidTopicNames) {
      final ConnectRecord in = new SourceRecord(null, null, null, null, invalidTopicName, null, null);
      final ConnectRecord out = smt.apply(in);
      Assert.assertNull(out);
    }
  }

  @Test
  public void validTopicWithOnePartitionShouldReturnPartition0() {
    final Map<String, Object> props = new HashMap<>();
    props.put("partitions", 1);
    smt.configure(props);

    String[] topicsWithDifferentEoNID = new String [] {
      "spBv1.0/PAULv0/DDATA/E01001UOKC3EQ4U/0-0",
      "spBv1.0/PAULv0/DDATA/E01001UOKC3EQ4A/0-1",
      "spBv1.0/PAULv0/DDATA/E01001UOKC3EQ4B/0-2",
      "spBv1.0/PAULv0/DDATA/E01001UOKC3EQ4d/0-3",
    };

    for(String topicWithDifferentEoNID : topicsWithDifferentEoNID) {
      final ConnectRecord in = new SourceRecord(null, null, null, null, topicWithDifferentEoNID, null, null);
      final ConnectRecord out = smt.apply(in);
      Assert.assertEquals(0, out.kafkaPartition().intValue());
    }
  }

  @Test
  public void validTopics2000PartitionShouldReturnSamePartitionforSameEoNId() {
    final Map<String, Object> props = new HashMap<>();
    props.put("partitions", 2000);

    smt.configure(props);

    String[] topicsWithSameEoNId = new String [] {
      "spBv1.0/PAULv1/DDATA/E01001UOKC3EQ4U/0-0",
      "spBv1.0/PAULv3/DDATA/E01001UOKC3EQ4U/0-1",
      "spBv1.0/PAULv2/DDATA/E01001UOKC3EQ4U/0-2",
      "spBv1.0/PAULv0/DDATA/E01001UOKC3EQ4U",
      "spBv1.0/PAULv1/NDATA/E01001UOKC3EQ4U",
      "spBv1.0/PAULv3/NDATA/E01001UOKC3EQ4U",
      "spBv1.0/PAULv2/NDATA/E01001UOKC3EQ4U",
      "spBv1.0/PAULv0/NDATA/E01001UOKC3EQ4U/0-3",
      "spBv1.0/PAULv1/DDATA/E01001UOKC3EQ4U/0-0",
      "spBv1.0/PAULv3/DDATA/E01001UOKC3EQ4U/0-1",
      "spBv1.0/PAULv2/DDATA/E01001UOKC3EQ4U/0-2",
      "spBv1.0/PAULv0/DDATA/E01001UOKC3EQ4U/0-3",
      "spBv1.0/PAULv1/NDATA/E01001UOKC3EQ4U",
      "spBv1.0/PAULv3/NDATA/E01001UOKC3EQ4U",
      "spBv1.0/PAULv2/NDATA/E01001UOKC3EQ4U",
      "spBv1.0/PAULv0/NDATA/E01001UOKC3EQ4U",
    };

    Integer first = null;
    for(String topicWithSameEoNId : topicsWithSameEoNId) {
      final ConnectRecord in = new SourceRecord(null, null, null, null, topicWithSameEoNId, null, null);
      final ConnectRecord out = smt.apply(in);
      if (first == null) {
        first = out.kafkaPartition();
      } else {
        Assert.assertEquals(first, out.kafkaPartition());
      }
    }
  }


  @Test
  public void validTopicsWith2000PartitionShouldReturnDifferntPartitionforSameEoNId() {
    final Map<String, Object> props = new HashMap<>();
    props.put("partitions", 2000);

    smt.configure(props);

    String[] topicsWithSameEoNId = new String [] {
      "spBv1.0/PAULv1/DDATA/E01001UOKC3EQ40/0-0",
      "spBv1.0/PAULv1/DDATA/E01001UOKC3EQ41/0-0",
      "spBv1.0/PAULv1/DDATA/E01001UOKC3EQ42/0-0",
      "spBv1.0/PAULv1/DDATA/E01001UOKC3EQ43/0-0",
      "spBv1.0/PAULv1/DDATA/E01001UOKC3EQ44/0-0",
      "spBv1.0/PAULv1/DDATA/E01001UOKC3EQ45/0-0",
      "spBv1.0/PAULv1/DDATA/E01001UOKC3EQ46/0-0",
      "spBv1.0/PAULv1/DDATA/E01001UOKC3EQ47/0-0",
      "spBv1.0/PAULv1/DDATA/E01001UOKC3EQ48/0-0",
      "spBv1.0/PAULv1/DDATA/E01001UOKC3EQ49/0-0",
      "spBv1.0/PAULv1/DDATA/E01001UOKC3EQ4A/0-0",
      "spBv1.0/PAULv1/DDATA/E01001UOKC3EQ4B/0-0",
    };

    int[] expected = new int[] {
      134,
      1286,
      736,
      711,
      946,
      1034,
      182,
      340,
      156,
      338,
      216,
      176
    };
    
    for(int i = 0; i < topicsWithSameEoNId.length; i++) {
      final String topicString = topicsWithSameEoNId[i];
      final ConnectRecord in = new SourceRecord(null, null, null, null, topicString, null, null);
      final ConnectRecord out = smt.apply(in);
      Assert.assertEquals( expected[i], out.kafkaPartition().intValue());
    }
  }
}