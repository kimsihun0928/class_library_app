package com.tenco.library.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Book {

    private int id;
    private String title;
    private String author;
    private String publisher;
    private int publicationYear;
    private String isbn;
    private boolean available;

}
