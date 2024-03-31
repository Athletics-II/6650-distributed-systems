package org.milestone3.albumServlet.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class MessageConsumerService {
    private final MessageDAO messageDao;
    private final ExecutorService executorService;
    private Connection connection;
    private final ObjectMapper objectMapper;
    private final static String QUEUE_POST_LIKE = "post_like";
    private final static String QUEUE_REPLY = "post_response";

    @Autowired
    public MessageConsumerService(MessageDAO messageDao, ObjectMapper objectMapper) {
        this.messageDao = messageDao;
        this.objectMapper = objectMapper;
        this.executorService = Executors.newFixedThreadPool(4); // Adjust thread pool size as needed
    }

    @PostConstruct
    public void init() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            this.connection = factory.newConnection();
            executorService.submit(() -> consumeMessages(QUEUE_POST_LIKE));
        } catch (IOException | TimeoutException e) {
            Logger.getLogger(MessageConsumerService.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void consumeMessages(String queueName) {
        try {
            final Channel channel = connection.createChannel();
            channel.queueDeclare(queueName, true, false, false, null);
            channel.basicQos(1); // Handle one message at a time
            System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
                if (QUEUE_POST_LIKE.equals(queueName)) {
                    Map<String, String> messageData = objectMapper.readValue(message, Map.class);
                    String requestID = messageData.get("requestID");
                    String likeOrNot = messageData.get("likeornot");
                    String albumID = messageData.get("albumID");
                    processAlbumLike(requestID, likeOrNot, albumID);
                }
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };
            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {});
        } catch (IOException e) {
            Logger.getLogger(MessageConsumerService.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void processAlbumLike(String requestID, String likeOrNot, String albumID) {
        int response = messageDao.processMessagePostLike(likeOrNot, Integer.parseInt(albumID));
        publishToReplyQueue(requestID, response);
    }

    private void publishToReplyQueue(String requestID, int likeResponse) {
        try (Channel channel = connection.createChannel()) {

            Map<String, Object> messageContent = new HashMap<>();
            messageContent.put("type", "postLikeResponse");
            messageContent.put("requestID", requestID);
            if (likeResponse == 1) {
                messageContent.put("status", "success");
            } else {
                messageContent.put("status", "error");
            }
            String messageJson = objectMapper.writeValueAsString(messageContent);
            channel.basicPublish("", QUEUE_REPLY, null, messageJson.getBytes(StandardCharsets.UTF_8));
            System.out.println("Published to reply queue: " + messageJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void cleanUp() throws IOException {
        if (this.connection != null) {
            this.connection.close();
        }
        executorService.shutdown();
    }
}
