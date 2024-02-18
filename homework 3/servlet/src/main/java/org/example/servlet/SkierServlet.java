package org.example.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import org.json.JSONObject;


public class SkierServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)
        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            res.setStatus(HttpServletResponse.SC_OK);
            System.out.println("Skier "+urlParts[7]+" has a new lift ride on day "+urlParts[5]+" season "+urlParts[3]+" at resort "+urlParts[1]);
            res.getWriter().write("It works!");
        }
    }

    private boolean isUrlValid(String[] urlParts) {
        // Check if the URL parts match the expected pattern
        if (urlParts.length != 8) {
            // The URL should have 8 parts including the empty string at the start
            return false;
        }
        try {
            int resortID = Integer.parseInt(urlParts[1]);
            int seasonYear = Integer.parseInt(urlParts[3]);
            int dayNumber = Integer.parseInt(urlParts[5]);
            int skierID = Integer.parseInt(urlParts[7]);
        } catch (NumberFormatException e) {
            // If any part is not an integer, the URL is not valid
            return false;
        }
        return true;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String urlPath = req.getPathInfo();
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing parameters");
            return;
        }
        String[] urlParts = urlPath.split("/");
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        String requestBody = stringBuilder.toString();
        JSONObject jsonObject = new JSONObject(requestBody);
        int time = jsonObject.getInt("time");
        int liftID = jsonObject.getInt("liftID");
        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        int resortID = Integer.parseInt(urlParts[1]);
        int seasonYear = Integer.parseInt(urlParts[3]);
        int dayNumber = Integer.parseInt(urlParts[5]);
        int skierID = Integer.parseInt(urlParts[7]);
        boolean success = updateDatabase(resortID, seasonYear, dayNumber, skierID, time, liftID);

        if (success) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("Entry created/updated successfully");
        } else {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("Error processing request");
        }
    }

    private boolean updateDatabase(int resortID, int seasonYear, int dayNumber, int skierID, int time, int liftID) {
        LiftRideDao liftRideDao = new LiftRideDao();
        LiftRide newLiftRide = new LiftRide(skierID, resortID, seasonYear, dayNumber, time, liftID);
        try {
            liftRideDao.createLiftRide(newLiftRide);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
