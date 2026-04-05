package com.english.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.common.utils.UserHolder;
import com.english.user.domain.dto.UserInfoDTO;
import com.english.user.domain.po.UserInfo;
import com.english.user.domain.vo.Result;
import com.english.user.mapper.UserInfoMapper;
import com.english.user.service.IUserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService  {

    @Override
    public Result updateUserInfo(UserInfoDTO userInfoDTO) {
        UserInfo userInfo = BeanUtil.copyProperties(userInfoDTO, UserInfo.class);
        userInfo.setId(UserHolder.getUserId()); // 设置主键
        userInfo.setUpdateTime(LocalDateTime.now());
        boolean isSuccess = updateById(userInfo);
        if (!isSuccess) {
            return Result.fail("更新用户信息失败");
        }
        return Result.ok();
    }
}
