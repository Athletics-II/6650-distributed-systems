package org.milestone3.albumServlet.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.milestone3.albumServlet.AlbumDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
public class AlbumPublisherController {

    private final AlbumPublisherService albumPublisherService;

    @Autowired
    public AlbumPublisherController(AlbumPublisherService albumPublisherService) {
        this.albumPublisherService = albumPublisherService;
    }

    // todo: construct albumdto instance
    @PostMapping("/albums")
    public ResponseEntity<Map<String, Object>> createAlbum(@RequestParam("image") MultipartFile image,
                                                           @RequestPart("profile") AlbumDTO albumDTO)
            throws IOException, InterruptedException {

        Map<String, Object> postResponse = albumPublisherService.processPostImageRequest(image, albumDTO);
        return ResponseEntity.ok(postResponse);
    }

    @PostMapping("/review/{likeornot}/{albumID}")
    public ResponseEntity<String> reviewAlbum(@PathVariable("likeornot") String likeOrNot,
                                              @PathVariable("albumID") String albumID)
            throws JsonProcessingException, InterruptedException {
        albumPublisherService.publishToQueuePostLike(likeOrNot, albumID);
        return ResponseEntity.ok("successful");
    }
}
