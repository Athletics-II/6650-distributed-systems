package org.milestone3.albumServlet.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class MessageQueueClient {
    private static final String QUEUE_POST_LIKE = "post_like";
    private static final String QUEUE_REPLY = "post_response";
    private final Connection connection;
    private final BlockingQueue<Channel> channelPool;
    private final ObjectMapper objectMapper;

    public MessageQueueClient(@Value("${message.queue.poolSize}")int poolSize, ObjectMapper objectMapper) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        this.connection = factory.newConnection();

        this.channelPool = new LinkedBlockingQueue<>(poolSize);
        for (int i=0; i<poolSize; i++) {
            Channel channel = connection.createChannel();
            this.channelPool.add(channel);
        }
        this.objectMapper = objectMapper;
    }

    public void publishToQueuePostLike(String likeOrNot, String albumID, String requestId)
            throws JsonProcessingException, InterruptedException {
        String messageJson = objectMapper.writeValueAsString(
                Map.of(
                        "requestID", requestId,
                        "likeornot", likeOrNot,
                        "albumID", albumID
                )
        );
        publishMessage(messageJson);
    }

    private void publishMessage(String message) throws InterruptedException {
        // block until a channel is available
        Channel channel = channelPool.take();
        try {
            channel.queueDeclare(MessageQueueClient.QUEUE_POST_LIKE, true, false, false, null);
            channel.basicPublish("", MessageQueueClient.QUEUE_POST_LIKE, null, message.getBytes());
            // for debugging
            System.out.println("Message sent to " + MessageQueueClient.QUEUE_POST_LIKE + "; Message: " + message + " \n");
        } catch (IOException e) {
            Logger.getLogger(MessageQueueClient.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            // return the channel to the pool
            channelPool.put(channel);
        }
    }

    public void listenToReplyQueue() throws InterruptedException {
        Channel channel = channelPool.take(); // Take a channel from the pool
        try {
            channel.queueDeclare(QUEUE_REPLY, false, false, false, null);
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                try {
                    Map<String, Object> messageContent = objectMapper.readValue(message, Map.class);
                    String requestID = (String) messageContent.get("requestID");
                    String status = (String) messageContent.get("status");
                    // Handle the message as needed
                    System.out.println("Received reply for requestID " + requestID + ": " + status);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            channel.basicConsume(QUEUE_REPLY, true, deliverCallback, consumerTag -> {});
        } catch (IOException e) {
            e.printStackTrace();
            channelPool.put(channel); // Put the channel back in the pool in case of failure
        }
    }

    public void close() throws IOException {
        while (!channelPool.isEmpty()) {
            try {
                Channel channel = channelPool.take();
                if (channel.isOpen()) {
                    channel.close();
                }
            } catch (InterruptedException | TimeoutException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
        if (this.connection != null) {
            this.connection.close();
        }
    }
}
