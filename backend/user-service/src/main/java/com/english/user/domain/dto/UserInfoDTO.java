package com.english.user.domain.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserInfoDTO {

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 性别
     */
    private String gender;

    /**
     * 生日
     */
    private LocalDate birthday;

}
