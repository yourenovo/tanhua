package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Friend;

import java.util.List;

public interface FriendApi {
    void save(Long userId, Long friendId);

    List<Friend> findByUserId(Long userId, Integer page, Integer pagesize);
}
