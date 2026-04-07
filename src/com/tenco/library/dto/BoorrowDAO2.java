package com.tenco.library.dto;

import com.tenco.library.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BoorrowDAO2 {

    public void borrowBook(int bookId, int studentId) throws SQLException {
        Connection conn = null;

        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            String checkSql = """
                    SELECT available FROM books WHERE id = ?
                    """;
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, bookId);

                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next() == false) {
                        throw new SQLException("존재하지 않는 도서입니다.");
                    }

                    if (rs.getBoolean("available") == false) {
                        throw new SQLException("현재 대출중인 도서압니다. 반납 후 이용가능");
                    }

                }

            } // end of checkPstmt

            String borrowSql = """
                    INSERT INTO borrows (book_id, student_id, borrow_date) VALUES (?, ?, ?)
                    """;

            try (PreparedStatement borrowPstmt = conn.prepareStatement(borrowSql)) {
                borrowPstmt.setInt(1, bookId);
                borrowPstmt.setInt(2, studentId);
                borrowPstmt.setDate(3, Date.valueOf(LocalDate.now()));
                borrowPstmt.executeUpdate();
            } // end of borrowPstmt

            // 3. 도서 상태 변경
            String updateSql = """
                    UPDATE books SET available = FALSE WHERE id = ?
                    """;

            try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                updatePstmt.setInt(1, bookId);
                updatePstmt.executeUpdate();
            } // end of updatePstmt

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("오류발생");
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public List<Borrow> getBorrowBooks() throws SQLException {
        List<Borrow> borrowList = new ArrayList<>();
        String sql = """
                SELECT * FROM books WHERE return_date IS NULL ORDER BY borrow_date
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Borrow borrow = Borrow.builder()
                        .id(rs.getInt("id"))
                        .bookId(rs.getInt("book_id"))
                        .studentId(rs.getInt("student_id"))
                        .borrowDate(
                                rs.getDate("borrow_date") != null ? rs.getDate("borrow_date").toLocalDate() : null)
                        .build();

                borrowList.add(borrow);
            }
        }
        return borrowList;

    }

    public void returnBook(int bookId, int studentId) {
        Connection conn = null;

        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            String checkSql = """
                    SELECT id FROM borrows WHERE book_id = ? AND student_id = ?
                    AND return_date IS NULL
                    """;

            int borrowId;
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, bookId);
                checkPstmt.setInt(2, studentId);

                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next() == false) {
                    throw new SQLException("대출 기록 없");
                    }
                    borrowId = rs.getInt("id");
                }

            } // end of checkPstmt

            String updateBorrowSql = """
                    UPDATE borrows SET return_date = ? WHERE id = ?
                    """;

            try ( PreparedStatement recordPstmt = conn.prepareStatement(updateBorrowSql)) {
                recordPstmt.setDate(1, Date.valueOf(LocalDate.now()));
                recordPstmt.setInt(2, borrowId);
                recordPstmt.executeUpdate();

            } // end of recordPstmt

            String updateBookSql = """
                    UPDATE books SET available = ture WHERE id = ?
                    """;

            try (PreparedStatement updateBookPstmt = conn.prepareStatement(updateBookSql)) {
                updateBookPstmt.setInt(1, bookId);
                updateBookPstmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
