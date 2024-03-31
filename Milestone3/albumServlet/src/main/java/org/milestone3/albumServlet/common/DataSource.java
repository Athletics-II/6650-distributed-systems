package org.milestone3.albumServlet.common;

import org.apache.commons.dbcp2.*;
public class DataSource {
    private static BasicDataSource dataSource;
    private static final String HOST_NAME = System.getProperty("MySQL_IP_ADDRESS");
    private static final String PORT = System.getProperty("MySQL_PORT");
    private static final String DATABASE = "Album";
    private static final String USERNAME = System.getProperty("DB_USERNAME");
    private static final String PASSWORD = System.getProperty("DB_PASSWORD");

    static {
        dataSource = new BasicDataSource();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
        dataSource.setUrl(url);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setInitialSize(10);
        dataSource.setMaxTotal(60);
    }

    public static BasicDataSource getDataSource() {
        return dataSource;
    }
}
