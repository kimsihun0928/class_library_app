package com.tenco.library.dao;

import com.tenco.library.dto.Borrow;
import com.tenco.library.util.DatabaseUtil;

import javax.xml.crypto.Data;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// 대출/반납 관련 SQL 을 실행하는 DAO
public class BorrowDAO {

    // 도서 대출 처리
    // 대출 가능 여부 확인 --> borrow 테이블에 기록 --- 북 테이블 0으로 변경
    // try-with-resource 블록 문법 - 블록이 끝나는 순간 무조건 자원을 먼저 닫아버림
    // 이게 트랜잭션 처리할 때는 값을 확인해서 commit 혹은 rollback 을 해야하기 때문에 사용 X
    // 즉, 직접 close() 처리 해야함 - 트랜잭션 처리를 위해

    /**
     *
     * @param bookId
     * @param studentId : 학번이 아니라 student 테이블의 pk 이다.  int 형
     * @throws SQLException
     */

    public void borrowBook(int bookId, int studentId) throws SQLException {
        Connection conn = null;

        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작 .....

            // 1. 대출 가능 여부 확인
            String checkSql = """
                    SELECT available FROM books WHERE id = ?
                    """;
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, bookId);

                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next() == false) {
                        throw new SQLException("존재하지 않는 도서입니다. : " + bookId);
                    }

                    if (rs.getBoolean("available") == false) {
                        throw new SQLException("현재 대출중인 도서입니다. 반납 후 이용 가능");
                    }
                }
            } // end of checkPstmt

            // 대출 가능한 상태 --> 대출 테이블에 학번, 책 번호를 기록해야함.
            // 2. 대출 기록 추가
            String borrowSql = """
                    INSERT INTO borrows (book_id, student_id, borrow_date) VALUES (?, ?, ?)
                    """;

            try (PreparedStatement borrowPstmt = conn.prepareStatement(borrowSql)) {
                borrowPstmt.setInt(1, bookId);
                borrowPstmt.setInt(2, studentId);
                // LocalDate --> Date 타입으로 변환
                borrowPstmt.setDate(3, Date.valueOf(LocalDate.now()));
                borrowPstmt.executeUpdate();
            } // end of borrowPstmt

            // 3. 도서 상태 변경(대출 불가)
            String updateSql = """
                    UPDATE books SET available = FALSE WHERE id = ?
                    """;

            try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                updatePstmt.setInt(1, bookId);
                updatePstmt.executeUpdate();
            } // end of updatePstmt

            // 1, 2, 3, 모두 성공 -> 커밋
            conn.commit();


        } catch (
                SQLException e) {
            if (conn != null) {
                conn.rollback(); // 하나라도 실패하면 전체 롤백
            }
            System.out.println("오류 발생 : " + e.getMessage());
        } finally {
            if (conn != null) {
                // 혹시 중간에 오류가 나서 처리가 안된다면 롤백 처리함
                // conn.rollback(); -- 성공하더라도 무조건 롤백하게됨 .. 그럼 반영 안됨
                conn.setAutoCommit(true); // autocommit 복구
                conn.close();
            }
        }
    }

    // 현재 대출 중인 도서 목록 조회
    public List<Borrow> getBorrowedBooks() throws SQLException {
        List<Borrow> borrowList = new ArrayList<>();
        String sql = """
                SELECT * FROM borrows WHERE return_date IS NULL ORDER BY borrow_date
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Borrow borrow = Borrow.builder()
                        .id(rs.getInt("id"))
                        .bookId(rs.getInt("book_id"))
                        .studentId(rs.getInt("student_id"))
                        // rs.getDate() --> toLocalDate() --> LocalDate 타입으로 변환됨
                        .borrowDate(
                                rs.getDate("borrow_date") != null
                                        ? rs.getDate("borrow_date").toLocalDate()
                                        : null
                        )
                        .build();

                borrowList.add(borrow);
            }
        }
        return borrowList;
    }


    // 도서 반납 처리
    // 대출 기록 확인 --> return_date 업데이트 --> Book 도서 상태 업데이트


    /**
     *
     * @param bookId
     * @param studentId : student 테이블 pk
     */
    public void returnBook(int bookId, int studentId) throws SQLException {
        // 트랜잭션 시작
        Connection conn = null;

        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // 트랙잭션 시작

            // 1. 대출 가능 여부 확인
            String checkSql = """
                    
                        SELECT id FROM borrows 
                    WHERE book_id = ? AND student_id = ? 
                    AND return_date IS NULL
                    """;
            int borrowId;
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, bookId);
                checkPstmt.setInt(2, studentId);

                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next() == false) {
                        throw new SQLException("대출 기록이 없거나 이미 반납되었습니다.");
                    }
                    // 대출 테이블에 해당하는 PK 추출
                    borrowId = rs.getInt("id");
                }
            } // end of checkPstmt

            // 2. 대출 테이블에 반납일 기록
            String updateBorrowSql = """
                    UPDATE borrows SET return_date = ? WHERE id = ?
                    """;

            try (PreparedStatement recordPstmt = conn.prepareStatement(updateBorrowSql)) {
                recordPstmt.setDate(1, Date.valueOf(LocalDate.now()));
                recordPstmt.setInt(2, borrowId);
                recordPstmt.executeUpdate();
            } // end of recordPstmt

            // 3. 도서 상태 변경 (대출 가능)
            String updateBookSql = """
                    UPDATE books SET available = true WHERE id = ?
                    """;

            try (PreparedStatement updateBookPstmt = conn.prepareStatement(updateBookSql)) {
                updateBookPstmt.setInt(1, bookId);
                updateBookPstmt.executeUpdate();
            } // end of updatePstmt

            // 1, 2, 3, 모두 성공 -> 커밋
            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("오류 발생 " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
        // 트랜잭션 종료
    }

    // 테스트 코드 작성
    public static void main(String[] args) {
        // 샘플데이터 - 20230001
        BorrowDAO borrowDAO = new BorrowDAO();
        try {
            // borrowDAO.borrowBook(1, 1);
            // List<Borrow> borrowList = borrowDAO.getBorrowedBooks();
            // System.out.println(borrowList);
            borrowDAO.returnBook(1, 1);

        } catch (SQLException e) {
            System.out.println("------------");
            System.out.println("오류 발생 : " + e.getMessage());
        }
    }
}
