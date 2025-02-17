package com.tanhua.server.service;

import com.tanhua.dubbo.api.UserLocationApi;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.server.Interceptor.UserHolder;
import com.tanhua.server.exception.BusinessException;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class BaiduService {

    @DubboReference
    private UserLocationApi userLocationApi;
    public void updateLocation(Double longitude, Double latitude, String address) {
        Boolean aBoolean = userLocationApi.updateLocation(UserHolder.getUserId(), longitude, latitude, address);
        if(!aBoolean){
            throw new BusinessException(ErrorResult.error());
        }
    }
}
