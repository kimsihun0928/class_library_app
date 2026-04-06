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
    private LocalDate borrowDate;
    private LocalDate returnDate;

}
