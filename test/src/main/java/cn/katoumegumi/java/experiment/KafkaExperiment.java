package cn.katoumegumi.java.experiment;

/*import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;*/

public class KafkaExperiment {
    /*public static void main(String[] args) {
        Thread thread = new Thread(()->{
            inputKafkaProducer();
        });
        Thread thread1 = new Thread(()->{
           printfKafkaConsumer();
        });
        //thread.setName("生产者");
        thread1.setName("消费者");
        thread.start();
        thread1.start();
    }


    public static void inputKafkaProducer(){
        Map<String,Object> map = new HashMap<>();
        map.put("bootstrap.servers","192.168.0.111:9092");
        map.put("acks","all");
        map.put("retries",0);
        map.put("batch.size",16384);
        map.put("linger.ms",1);
        map.put("buffer.momory",1024L*1024L*1024L);
        map.put("key.serializer", StringSerializer.class.getName());
        map.put("value.serializer",StringSerializer.class.getName());
        KafkaProducer<String,String> kafkaProducer = new KafkaProducer<String, String>(map);
        kafkaProducer.partitionsFor("wstest");
        Integer i = 0;
        while (true){
            //kafkaProducer.initTransactions();
            //kafkaProducer.beginTransaction();
            long time = 0L;
            if(i == 5){
                time = 50*1000L;
            }
            ProducerRecord<String,String> producerRecord = new ProducerRecord<String, String>(
                    "wstest",
                    0,
                    System.currentTimeMillis()+time, i.toString(),
                    "这个是第" + i + "条数据");
            kafkaProducer.send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    System.out.println(recordMetadata.offset());
                    System.out.println(recordMetadata.topic());
                    System.out.println(recordMetadata.toString());
                    System.out.println(recordMetadata.timestamp());
                }
            });
            //kafkaProducer.commitTransaction();
            i++;
            if(i%10 == 0){
                try {
                    Thread.sleep(Duration.ofSeconds(10).toMillis());
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

            }
        }
    }


    public static void printfKafkaConsumer(){
        Map<String,Object> map = new HashMap<>();
        *//*map.put("bootstrap.servers","192.168.0.111:9092");
        map.put("acks","all");
        map.put("retries",0);
        map.put("batch.size",16384);
        map.put("linger.ms",1);
        map.put("buffer.momory",1024l*1024l*1024l);
        map.put("key.serializer", StringSerializer.class.getName());
        map.put("value.serializer",StringSerializer.class.getName());*//*
        map.put("bootstrap.servers", "192.168.0.111:9092");
        map.put("group.id", "wstest");
        map.put("enable.auto.commit", "true");
        map.put("auto.commit.interval.ms", "1000");
        map.put("auto.offset.reset", "earliest");
        map.put("session.timeout.ms", "30000");
        map.put("key.deserializer", StringDeserializer.class.getName());
        map.put("value.deserializer", StringDeserializer.class.getName());
        KafkaConsumer<String,String> kafkaConsumer = new KafkaConsumer<String,String>(map);
        kafkaConsumer.subscribe(Arrays.asList("wstest"));
        while (true){
            ConsumerRecords<String,String> consumerRecords = kafkaConsumer.poll(Duration.ofSeconds(1));
            for(ConsumerRecord<String,String> consumerRecord:consumerRecords){
                System.out.println(consumerRecord.key());
                System.out.println(consumerRecord.value());
            }

        }
    }*/
}
