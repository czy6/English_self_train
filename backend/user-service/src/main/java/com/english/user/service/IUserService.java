package com.english.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.english.user.domain.po.User;
import com.english.user.domain.dto.LoginFormDTO;
import com.english.user.domain.vo.Result;

public interface IUserService extends IService<User> {
    Result sendCode(String phone);

    Result login(LoginFormDTO loginForm);

    Result getUserInfo();

    User createUserByPhone(String phone);
}
