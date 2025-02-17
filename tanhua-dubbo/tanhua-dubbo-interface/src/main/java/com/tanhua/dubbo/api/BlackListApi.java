package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.model.domain.UserInfo;

public interface BlackListApi {

    IPage<UserInfo> findByUserId(Long userId, int page, int size);



    void deleteBlackList(Long userId, Long blackUserId);
}