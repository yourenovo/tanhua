package com.tanhua.server.service;

import com.tanhua.dubbo.api.UserApi;
import com.tanhua.model.domain.User;
import com.tanhua.model.vo.HuanXinUserVo;
import com.tanhua.server.Interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class HuanXinService {
    @DubboReference
    UserApi userApi;

    public HuanXinUserVo findHuanXinUser() {
        Long userId = UserHolder.getUserId();
        User user = userApi.findById(userId);
        return new HuanXinUserVo(user.getHxUser(), user.getHxPassword());

    }
}
