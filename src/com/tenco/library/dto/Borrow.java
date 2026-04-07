package com.tenco.library.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Borrow {
    private int id;
    private int bookId;
    private int studentId;
    private LocalDate borrowDate; // SQL 에선 DATE 형식으로 넘어옴
    private LocalDate returnDate;

}
