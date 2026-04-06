package com.tenco.library.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    private static final String URL = "jdbc:mysql://localhost:3306/library?serverTimezone=Asia/Seoul";
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    // 새로운 DB 연결 객체를 반환합니다.
    public static Connection getConnection() throws SQLException {
        // 재미삼아 효과 만들어보기
        Thread thread = new Thread(() -> {
            System.out.print("Connecting to database ");
            for (int i = 0; i < 5; i++) {
                System.out.print(".");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Connection connection = DriverManager.getConnection(URL,DB_USER,PASSWORD);
        System.out.println();
        System.out.println(connection.getMetaData().getDatabaseProductName());
        System.out.println(connection.getMetaData().getDatabaseProductVersion());

        return connection;
    }

    // TODO - 삭제 예정
    // 테스트 코드 작성
    public static void main(String[] args) {

        try {
            DatabaseUtil.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
