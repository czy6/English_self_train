package com.english.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.english.user.domain.dto.UserInfoDTO;
import com.english.user.domain.po.UserInfo;
import com.english.user.domain.vo.Result;

public interface IUserInfoService extends IService<UserInfo> {
    Result updateUserInfo(UserInfoDTO userInfo);
}
