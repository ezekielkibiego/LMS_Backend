package com.zeraki.co.ke.lms.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    // Update the DB_URL, DB_USER, and DB_PASSWORD to connect to the remote database
    private static final String DB_URL = "jdbc:postgresql://dpg-cnceouicn0vc73f1c8jg-a.oregon-postgres.render.com/lms_0k07";
    private static final String DB_USER = "lms_0k07_ezekielkibiego";
    private static final String DB_PASSWORD = "NoyxR2mDrrFD5E1JnOYjPDSAFxfirOjG";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
