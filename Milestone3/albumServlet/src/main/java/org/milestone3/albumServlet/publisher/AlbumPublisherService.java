package org.milestone3.albumServlet.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.milestone3.albumServlet.AlbumDTO;
import org.milestone3.albumServlet.common.MessageQueueClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AlbumPublisherService {
    private final MessageQueueClient messageQueueClient;
    private final AlbumDAO albumDAO;

    @Autowired
    public AlbumPublisherService(MessageQueueClient messageQueueClient, AlbumDAO albumDAO) {
        this.messageQueueClient = messageQueueClient;
        this.albumDAO = albumDAO;
    }

    public int processPostImageRequest(long imageSize, AlbumDTO profile) {
        return albumDAO.createAlbum(profile, imageSize);
    }

    public String publishToQueuePostLike(String likeOrNot, String albumID)
            throws JsonProcessingException, InterruptedException {
        String requestId = UUID.randomUUID().toString();
        messageQueueClient.publishToQueuePostLike(likeOrNot, albumID, requestId);
        return requestId;
    }

    public Map<String, String> processGetReviewRequest(int albumID) {
        int[] results = albumDAO.getLikeByAlbumID(albumID);
        Map<String, String> response = new HashMap<>();
        response.put("type", "getReviewResponse");
        if (results == null) {
            response.put("status", "error");
            return response;
        }
        response.put("likes", String.valueOf(results[0]));
        response.put("dislikes", String.valueOf(results[1]));
        response.put("status", "success");
        return response;
    }
}
