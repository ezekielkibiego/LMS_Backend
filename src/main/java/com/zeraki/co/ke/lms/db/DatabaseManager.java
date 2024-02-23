package com.zeraki.co.ke.lms.db;

// db/DatabaseManager.java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseManager {
    private static final String DB_URL = "jdbc:postgresql://abul.db.elephantsql.com:5432/xnauyzyy";
    private static final String DB_USER = "xnauyzyy";
    private static final String DB_PASSWORD = "dTQMPutSbEiSLPlJ4DkLky0wAVTUIpTw";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

}



