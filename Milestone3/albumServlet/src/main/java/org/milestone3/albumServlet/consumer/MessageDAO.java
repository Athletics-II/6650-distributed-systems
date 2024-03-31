package org.milestone3.albumServlet.consumer;

import org.apache.commons.dbcp2.BasicDataSource;
import org.milestone3.albumServlet.common.DataSource;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Repository
public class MessageDAO {
    private static BasicDataSource dataSource;

    public MessageDAO() {
        dataSource = DataSource.getDataSource();
    }


    public int processMessagePostLike(String likeOrNot, int albumId) {
        String updateSql = "UPDATE Albums SET %s = %s + 1 WHERE albumID = ?";

        updateSql = String.format(updateSql, likeOrNot.equals("like") ? "likes" : "dislikes", likeOrNot.equals("like") ? "likes" : "dislikes");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

            pstmt.setInt(1, albumId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return 1; // Successfully updated
            } else {
                return 0; // No rows affected, indicating the albumId might not exist
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // Indicates an error occurred
        }

    }


}
