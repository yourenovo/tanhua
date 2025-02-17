package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.FocusUser;
import com.tanhua.model.mongo.Video;
import com.tanhua.model.vo.PageResult;

import java.util.List;

public interface VideoApi {

    String save(Video video);

    List<Video> queryVideoByVids(List<Long> vids);

    List<Video> queryVideoList(int i, Integer pageSize);

    void saveFollowUser(FocusUser followUser);

    void deleteFollowUser(Long userId, Long followUserId);

    PageResult findAllVideos(Integer page, Integer pagesize, Long uid);
}
