package org.milestone3.albumServlet.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.milestone3.albumServlet.AlbumDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class MessageConsumerService {
    private final MessageDao messageDao;
    private final ExecutorService executorService;
    private Connection connection;
    private final ObjectMapper objectMapper;
    private final static String QUEUE_POST_LIKE = "post_like";

    @Autowired
    public MessageConsumerService(MessageDao messageDao, ObjectMapper objectMapper) {
        this.messageDao = messageDao;
        this.objectMapper = objectMapper;
        this.executorService = Executors.newFixedThreadPool(4); // Adjust thread pool size as needed
    }

    @PostConstruct
    public void init() {
        // Initialize connection and start consumers
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
                    String likeOrNot = messageData.get("likeornot");
                    String albumID = messageData.get("albumID");
                    processAlbumLike(likeOrNot, albumID);
                }
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };
            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {});
        } catch (IOException e) {
            Logger.getLogger(MessageConsumerService.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void processAlbumImage(String albumDetailsJson, String imageBase64) {
        byte[] imageBinary = Base64.getDecoder().decode(imageBase64);
        try {
            AlbumDTO albumDto = objectMapper.readValue(albumDetailsJson, AlbumDTO.class);
            String name = albumDto.getName();
            String artist = albumDto.getArtist();
            String year = albumDto.getYear();
            int albumID = messageDao.processMessagePostImage(name, artist, year, imageBinary);
            publishToReplyQueue(albumID, null);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void publishToReplyQueue(Integer likeResponse) {
        try (Channel channel = connection.createChannel()) {
            // Construct the reply message
            Map<String, Object> messageContent = new HashMap<>();
            messageContent.put("type", "postLikeResponse");
            if (likeResponse == 0) {
                messageContent.put("status", "success");
            } else {
                messageContent.put("status", "error");
            }
//            if (albumID != null) {
//                messageContent.put("type", "postImageResponse");
//                if (albumID != -1) {
//                    messageContent.put("status", "success");
//                    messageContent.put("albumID", albumID);
//                } else {
//                    messageContent.put("status", "error");
//                    messageContent.put("albumID", "-1");
//                }
//            } else {}
            String messageJson = objectMapper.writeValueAsString(messageContent);
            channel.basicPublish("", QUEUE_REPLY, null, messageJson.getBytes(StandardCharsets.UTF_8));
            System.out.println("Published to reply queue: " + messageJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processAlbumLike(String likeOrNot, String albumID) {
        String message = "";
        int response = messageDao.processMessagePostLike(message);
        publishToReplyQueue(response);
    }

    @PreDestroy
    public void cleanUp() throws IOException {
        if (this.connection != null) {
            this.connection.close();
        }
        executorService.shutdown();
    }
}
