package org.milestone3.albumServlet.publisher;

import org.apache.commons.dbcp2.BasicDataSource;
import org.milestone3.albumServlet.AlbumDTO;
import org.milestone3.albumServlet.common.DataSource;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class AlbumDAO {
    private static BasicDataSource dataSource;

    public AlbumDAO() {
        dataSource = DataSource.getDataSource();
    }

    public int createAlbum(AlbumDTO newAlbum, long imageSize) {
        String insertQuery = "INSERT INTO Albums (title, artist, year, imageSize) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, newAlbum.getTitle());
            preparedStatement.setString(2, newAlbum.getArtist());
            preparedStatement.setString(3, newAlbum.getYear());
            preparedStatement.setLong(4, imageSize);

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating album failed, no rows affected.");
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int albumID = generatedKeys.getInt(1);
                    return albumID;
                } else {
                    throw new SQLException("Creating album failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int[] getLikeByAlbumID(int albumID) {
        String query = "SELECT likes, dislikes FROM Albums WHERE albumID = ?";
        int[] result = new int[2];
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {

            preparedStatement.setInt(1, albumID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int likes = resultSet.getInt("likes");
                    int dislikes = resultSet.getInt("dislikes");
                    result[0] = likes;
                    result[1] = dislikes;
                    return result;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
