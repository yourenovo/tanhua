package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.model.vo.PageResult;

import java.util.List;

public interface RecommendUserApi {

    RecommendUser queryWithMaxScore(Long toUserId);

    PageResult queryRecommendUserList(Integer page, Integer pageSize, Long toUserId);

    RecommendUser queryByUserId(Long userId, Long toUserId);

    List<RecommendUser> queryCardsList(Long userId, int i);
}