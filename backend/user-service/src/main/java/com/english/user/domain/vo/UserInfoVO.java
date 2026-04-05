package com.english.user.domain.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserInfoVO {
    /**
     * 用户手机号
     */
    private String phone;

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

    /**
     * 账号等级
     */
    private Integer level;
}
