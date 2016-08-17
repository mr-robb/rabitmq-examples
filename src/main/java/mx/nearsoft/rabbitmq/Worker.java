package mx.nearsoft.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * Created by rfonseca on 8/16/16.
 */
public class Worker {
    private final static String QUEUE_NAME = "task_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        boolean durable = true;

        channel.queueDeclare(QUEUE_NAME, durable, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        channel.basicQos(1);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
                try {
                    doWork(message);
                }catch(InterruptedException ie){
                    System.err.println("Some error occurred while waiting");
                }
                finally {
                    System.out.println(" [x] Done");
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }

    private static void doWork(String task) throws InterruptedException {
        for (char ch: task.toCharArray()) {
            if (ch == '.') Thread.sleep(1000);
        }
    }
}
