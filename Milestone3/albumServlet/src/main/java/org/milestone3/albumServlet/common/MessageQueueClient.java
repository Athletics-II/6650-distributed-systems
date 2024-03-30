package org.milestone3.albumServlet.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.milestone3.albumServlet.AlbumMessagePayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

@Component
public class MessageQueueClient {
    private static final String QUEUE_POST_LIKE = "post_like";
    private final static String QUEUE_REPLY = "reply_queue";
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

//    public void publishToQueuePostImage(AlbumMessagePayload payload) throws JsonProcessingException, InterruptedException {
//        String imageBase64 = Base64.getEncoder().encodeToString(payload.getImageData());
//        String messageJson = objectMapper.writeValueAsString(
//                Map.of(
//                        "albumDetails", payload.getAlbumDetailsJson(),
//                        "imageData", imageBase64
//                )
//        );
//        publishMessage(QUEUE_POST_IMAGE, messageJson);
//        // todo: implement get album id
//    }



    public void publishToQueuePostLike(String likeOrNot, String albumID) throws JsonProcessingException, InterruptedException {
        String messageJson = objectMapper.writeValueAsString(
                Map.of(
                        "likeornot", likeOrNot,
                        "albumID", albumID
                )
        );
        publishMessage(QUEUE_POST_LIKE, messageJson);
    }

    private void publishMessage(String queueName, String message) throws InterruptedException {
        // block until a channel is available
        Channel channel = channelPool.take();
        try {
            channel.queueDeclare(queueName, true, false, false, null);
            channel.basicPublish("", queueName, null, message.getBytes());
            // for debugging
            System.out.println("Message sent to " + queueName);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // return the channel to the pool
            channelPool.put(channel);
        }
    }

    public void close() throws IOException {
        while (!channelPool.isEmpty()) {
            try {
                Channel channel = channelPool.take();
                if (channel != null && channel.isOpen()) {
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
