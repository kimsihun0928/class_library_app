package com.tenco.library.service;

import com.tenco.library.dao.AdminDAO;
import com.tenco.library.dao.BookDAO;
import com.tenco.library.dao.BorrowDAO;
import com.tenco.library.dao.StudentDAO;
import com.tenco.library.dto.Admin;
import com.tenco.library.dto.Book;
import com.tenco.library.dto.Borrow;
import com.tenco.library.dto.Student;

import java.sql.SQLException;
import java.util.List;

// 비즈니스 로직을 처리하는 서비스 클래스
// VIEW 계층 (화면) -> Service 계층으로 분류 --> Data 계층
// 뷰 계층에서는 DAO 를 직접 호출하지 말고 항상 Service 를 통해서 접근한다.
public class LibraryService {

    private final AdminDAO adminDAO = new AdminDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();

    // 만약 화면단에서 도서 추가 기능 요청 (화면단 코드) 발생한다면
    // 서비스 단에서는 사용자가 입력한 데이터가 유효한지 유효성 검사도 하고
    // 입력한 데이터가 정상적이라면 DB에 반영할 예정

    // 도서 추가 기능(제목, 저자 필수 검증)
    public void addBook(Book book) throws SQLException {
        // 1. 유효성 검사
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new SQLException("도서 제목은 필수 입력 항목입니다.");

        }
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new SQLException("도서 저자는 필수 입력 항목입니다.");
        }
        bookDAO.addBook(book);
    }

    // 전체 도서 목록 조회 (대출 여부 상관없이 다 출력)
    public List<Book> getAllBooks() throws SQLException {
        return bookDAO.getAllBooks();
    }

    // 책 제목으로 검색
    public List<Book> searchBooksByTitle(String title) throws SQLException {
        if (title == null || title.trim().isEmpty()) {
            throw new SQLException("검색어를 입력해주세요");
        }
        return bookDAO.searchBooksByTitle(title);
    }

    // 학생 등록 기능 (이름, 학번 필수 검증)
    public void addStudent(Student student) throws SQLException {
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new SQLException("학생의 이름은 필수 항목입니다.");
        }
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            throw new SQLException("학생의 학번은 필수 항목입니다.");
        }
        studentDAO.addStudent(student);
    }

    // 전체 학생 목록 조회
    public List<Student> getAllStudent() throws SQLException {
        return studentDAO.getAllStudents();
    }

    // 학번이 유효한지 조회(로그인 처리)

    /**
     *
     * @param studentId
     * @return
     * @throws SQLException
     */
    public Student authenticateStudent(String studentId) throws SQLException {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new SQLException("학번을 입력해주세요");
        }
        return studentDAO.authenticateStudent(studentId);
    }

    // 도서 대출 요청

    /**
     *
     * @param bookId
     * @param studentId
     * @throws SQLException
     */
    public void borrowBook(int bookId, int studentId) throws SQLException {
        if (bookId <= 0 || studentId <= 0) {
            throw new SQLException("유효한 도서 ID 와 학생 ID 를 입력해주세요.");
        }
        borrowDAO.borrowBook(bookId, studentId);
    }

    // 도서 반납 처리

    /**
     *
     * @param bookId
     * @param studentId
     * @throws SQLException
     */
    public void returnBook(int bookId, int studentId) throws SQLException {
        if (bookId <= 0 || studentId <= 0) {
            throw new SQLException("유효한 도서ID와 학생 ID를 입력해주세요");
        }
        borrowDAO.returnBook(bookId, studentId);
    }

    public List<Borrow> getBorrowedBooks() throws SQLException {
        return borrowDAO.getBorrowedBooks();
    }

    // 관리자 서비스 기능 추가
    public Admin authenticateAdmin(String adminId, String password) throws SQLException {

        if (adminId == null || adminId.trim().isEmpty()) {
            throw new SQLException("관리자 ID를 입력하세요");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new SQLException("관리자 password 를 입력하세요");
        }

        return adminDAO.authenticateAdmin(adminId, password);
    }

}