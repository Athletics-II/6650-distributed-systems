package album.albumServlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "AlbumServlet", value = "/albums/*")
@MultipartConfig
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
        String urlPath = request.getPathInfo();
        if (urlPath == null || urlPath.isEmpty()) {
            // Set response content type to JSON
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Retrieve the image part
            Part imagePart = request.getPart("image");
            long imageSize = imagePart.getSize(); // Get the size of the image

            // Generate an album ID (this example just uses a UUID)
            String albumID = java.util.UUID.randomUUID().toString();

            // Construct the response JSON
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("albumID", albumID);
            jsonResponse.addProperty("imageSize", imageSize);

            // Send the JSON response
            try (PrintWriter out = response.getWriter()) {
                out.println(jsonResponse.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
