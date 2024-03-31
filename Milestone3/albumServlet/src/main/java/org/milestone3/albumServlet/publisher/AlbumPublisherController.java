package org.milestone3.albumServlet.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.milestone3.albumServlet.AlbumDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@RestController
public class AlbumPublisherController {

    private final AlbumPublisherService albumPublisherService;

    @Autowired
    public AlbumPublisherController(AlbumPublisherService albumPublisherService) {
        this.albumPublisherService = albumPublisherService;
    }

    @PostMapping("/albums")
    public ResponseEntity<?> createAlbum(@RequestParam("image") MultipartFile image,
                                         @RequestPart("profile") String profileJson) {
        try {
            // Parse the JSON part to an AlbumDTO
            ObjectMapper objectMapper = new ObjectMapper();
            AlbumDTO albumInfo = objectMapper.readValue(profileJson, AlbumDTO.class);

            long imageSize = image.getSize();
            int albumID = albumPublisherService.processPostImageRequest(imageSize, albumInfo);

            if (albumID == -1) {
                return ResponseEntity.badRequest().body("Failed to create album");
            }

            return ResponseEntity.ok().body(Map.of(
                    "albumID", albumID,
                    "imageSize", imageSize
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to create album");
        }
    }

    @PostMapping("/review/{likeornot}/{albumID}")
    public ResponseEntity<String> reviewAlbum(@PathVariable("likeornot") String likeOrNot,
                                              @PathVariable("albumID") String albumID)
            throws JsonProcessingException, InterruptedException {
        if (Objects.equals(likeOrNot, "like") || Objects.equals(likeOrNot, "dislike")) {
            String requestId = albumPublisherService.publishToQueuePostLike(likeOrNot, albumID);
        } else {
            return ResponseEntity.badRequest().body("Invalid parameter");
        }
        return ResponseEntity.ok("Update request received");
    }

    @GetMapping("/review/{albumID}")
    public ResponseEntity<?> getAlbumReview(@PathVariable("albumID") String albumID) {
        Map<String, String> response = albumPublisherService.processGetReviewRequest(Integer.parseInt(albumID));
        if (Objects.equals(response.get("status"), "error")) {
            return ResponseEntity.badRequest().body("Failed to get album review");
        }
        return ResponseEntity.ok().body(Map.of(
                "likes", response.get("likes"),
                "dislikes", response.get("dislikes")
        ));
    }
}
