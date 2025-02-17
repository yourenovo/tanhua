package com.tanhua.dubbo.api;

import java.util.List;

public interface UserLocationApi {



    //更新地理位置
    Boolean updateLocation(Long userId, Double longitude, Double latitude, String address);

    List<Long> queryNearUser(Long userId, Long distance);
}