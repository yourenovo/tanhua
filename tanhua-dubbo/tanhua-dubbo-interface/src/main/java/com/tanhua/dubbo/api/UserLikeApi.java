package com.tanhua.dubbo.api;

public interface UserLikeApi {
    Boolean saveOrUpdate(Long userId, Long likeUserId, boolean b);
}
