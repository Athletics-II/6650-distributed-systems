package org.milestone3.albumServlet.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.milestone3.albumServlet.AlbumDTO;
import org.milestone3.albumServlet.AlbumMessagePayload;
import org.milestone3.albumServlet.common.MessageQueueClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class AlbumPublisherService {
    private final MessageQueueClient messageQueueClient;
    private final ObjectMapper objectMapper;
    @Autowired
    public AlbumPublisherService(MessageQueueClient messageQueueClient, ObjectMapper objectMapper) {
        this.messageQueueClient = messageQueueClient;
        this.objectMapper = objectMapper;
    }

//    public Map<String, Object> publishToQueuePostImage(MultipartFile image, AlbumDTO profile)
//            throws IOException, InterruptedException {
//        String albumDetailsJson = objectMapper.writeValueAsString(profile);
//        byte[] imageData = image.getBytes();
//
//        AlbumMessagePayload payload = new AlbumMessagePayload(albumDetailsJson, imageData);
//        messageQueueClient.publishToQueuePostImage(payload);
//        int albumID = messageQueueClient.consumeReplyQueue();
//        long imageSize = image.getSize();
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("albumID", albumID);
//        response.put("imageSize", imageSize);
//
//        return response;
//    }
    public Map<String, Object> processPostImageRequest(MultipartFile image, AlbumDTO profile) {
        
    }

    public void publishToQueuePostLike(String likeOrNot, String albumID)
            throws JsonProcessingException, InterruptedException {
        messageQueueClient.publishToQueuePostLike(likeOrNot, albumID);
    }
}
