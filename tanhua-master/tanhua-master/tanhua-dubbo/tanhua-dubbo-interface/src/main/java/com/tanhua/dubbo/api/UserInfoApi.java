package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.model.domain.UserInfo;

import java.util.List;
import java.util.Map;

public interface UserInfoApi {
    public void save(UserInfo userInfo);

    public void update(UserInfo userInfo);

    UserInfo findById(Long useId);

    Map<Long, UserInfo> findByIds(List<Long> ids, UserInfo userInfo);

    IPage<UserInfo> findAll(Integer page, Integer pagesize);
}
