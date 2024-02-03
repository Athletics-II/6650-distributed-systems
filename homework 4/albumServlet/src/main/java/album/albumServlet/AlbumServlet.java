package album.albumServlet;

import com.google.gson.Gson;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;

@WebServlet(name = "AlbumServlet", urlPatterns = "/albums/*")
public class AlbumServlet extends HttpServlet {
    private Gson gson = new Gson();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo(); // Get the path info (e.g., /{albumID})
        if (pathInfo == null || pathInfo.equals("/")) {
            res.setContentType("application/json");
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = res.getWriter();
            out.println("{\"msg\": \"Invalid request. Album ID is missing.\"}");
            return;
        }

        String albumID = pathInfo.substring(1); // Remove the leading "/"

        // For simplicity, we're just checking if it's not empty.
        if (albumID.trim().isEmpty()) {
            res.setContentType("application/json");
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = res.getWriter();
            out.println("{\"msg\": \"Invalid request. Album ID is invalid.\"}");
            return;
        }

        AlbumInfo newAlbumInfo = new AlbumInfo("Fall Out Boy", "So Much (For) Stardust", "2023");
        String albumJsonString = this.gson.toJson(newAlbumInfo);

        res.setContentType("application/json");
        res.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = res.getWriter();
        out.println(albumJsonString);
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder requestBody = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        // Parse the JSON body to the ImageUploadRequest class
        Gson gson = new Gson();
        ImageUploadRequest uploadRequest = gson.fromJson(requestBody.toString(), ImageUploadRequest.class);

        byte[] imageBytes = Base64.getDecoder().decode(uploadRequest.getImage());
        int imageSize = imageBytes.length;

        String albumID = java.util.UUID.randomUUID().toString();

        class ResponseObject {
            String albumID;
            int imageSize;

            public ResponseObject(String albumID, int imageSize) {
                this.albumID = albumID;
                this.imageSize = imageSize;
            }
        }

        ResponseObject responseObject = new ResponseObject(albumID, imageSize);

        // Set response headers
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        // Send the response
        try {
            response.getOutputStream().print(gson.toJson(responseObject));
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
