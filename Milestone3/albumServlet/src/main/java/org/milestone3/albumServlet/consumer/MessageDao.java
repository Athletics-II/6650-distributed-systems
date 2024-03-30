package org.milestone3.albumServlet.consumer;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Repository
public class MessageDao {
    private final Map<String, String> messageStore = new HashMap<>();
    private static BasicDataSource dataSource;

    public MessageDao() {
        dataSource = DataSource.getDataSource();
    }
    public int processMessagePostImage(String name, String artist, String year, byte[] image) {
        // SQL statement to insert a new album and return the generated key
        String sql = "INSERT INTO albums (name, artist, year, image) VALUES (?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setString(2, artist);
            pstmt.setString(3, year);
            pstmt.setBytes(4, image); // Assuming the image parameter is already a byte array

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating album failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Return the generated ID
                } else {
                    throw new SQLException("Creating album failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Indicate failure
    }

    public int processMessagePostLike(String message) {
        return 0;
    }

    public void save(String messageID, String messageContent) {
        messageStore.put(messageID, messageContent);
    }
}
