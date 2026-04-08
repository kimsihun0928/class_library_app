package com.tenco.library.dto;

import lombok.*;

// 관리자 데이터를 담는 DTO 클래스
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password") // toString 출력 시 비밀번호 제회
public class Admin {

    private int id;
    private String adminId;
    private String password;
    private String name;
}
