package com.tenco.library.dao;

import com.tenco.library.dto.Book;
import com.tenco.library.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    // 도서 추가
    public int addBook(Book book) throws SQLException {
        String sql = """
                INSERT INTO books(title, author, publisher, publication_year, isbn)
                values(?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseUtil.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getPublisher());
            pstmt.setInt(4, book.getPublicationYear());
            pstmt.setString(5, book.getIsbn());

            int rows = pstmt.executeUpdate();
            return rows;

        }
    }

    // 도서 전체 조회
    public List<Book> getAllBooks() throws SQLException {
        List<Book> bookList = new ArrayList<>();

        String sql = """
                SELECT * FROM books ORDER BY id;
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                bookList.add(mapToBook(rs));
            }

        }
        return bookList;

    }


    // 제목으로 도서 검색
    public List<Book> searchBooksByTitle(String title) throws SQLException {
        List<Book> bookList = new ArrayList<>();
        String sql = """
                SELECT * FROM books WHERE title LIKE ?
                """;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + title + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookList.add(mapToBook(rs));
                }
            }
        }
        return bookList;
    }

    private Book mapToBook(ResultSet rs) throws SQLException {

        return Book.builder()
                .id(rs.getInt("id"))
                .title(rs.getString("title"))
                .author((rs.getString("author")))
                .publisher(rs.getString("publisher"))
                .publicationYear(rs.getInt("publication_year"))
                .isbn(rs.getString("isbn"))
                .available(rs.getBoolean("available"))
                .build();
    }


    // 테스트 코드 작성
    public static void main(String[] args) {

        try {
            List<Book> resultList = new BookDAO().searchBooksByTitle("입문");
            System.out.println(resultList);
            System.out.println("----------------");
            System.out.println(new BookDAO().getAllBooks());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
