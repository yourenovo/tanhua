package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.model.domain.UserLike;
import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class RecommendApiImpl implements RecommendUserApi {
    @Autowired
    MongoTemplate mongoTemplate;

    public RecommendUser queryWithMaxScore(Long toUseId) {
        Criteria criteria = new Criteria().where("toUseId").is(toUseId);
        Query query = new Query(criteria).with(Sort.by(Sort.Order.desc("score"))).limit(1);
        return mongoTemplate.findOne(query, RecommendUser.class);
    }

    @Override
    public PageResult queryRecommendUserList(Integer page, Integer pageSize, Long toUserId) {
        //构建criteria
        Criteria criteria = new Criteria().where("toUseId").is(toUserId);
        //构建query对象
        Query query = new Query(criteria).with(Sort.by(Sort.Order.desc("score"))).limit((page - 1) * pageSize).skip(pageSize);
        long count = mongoTemplate.count(query, RecommendUser.class);
        List<RecommendUser> list = mongoTemplate.find(query, RecommendUser.class);
        return new PageResult(page, pageSize, (int) count, list);
    }

    @Override
    public RecommendUser queryByUserId(Long userId, Long toUserId) {
        Query query = new Query(new Criteria().where("userId").is(userId).and("toUserId").is(toUserId));
        RecommendUser one = mongoTemplate.findOne(query, RecommendUser.class);
        if (one == null) {
            //设置默认值
            one.setUserId(userId);
            one.setToUserId(toUserId);
            one.setScore(95d);
        }
        return one;
    }

    @Override
    public List<RecommendUser> queryCardsList(Long userId, int counts) {
        //查找推荐的列表排除喜欢和不喜欢的
        List<UserLike> userLikes = mongoTemplate.find(Query.query(Criteria.where("userId").is(userId)), UserLike.class);
        List<Long> likeUserId = CollUtil.getFieldValues(userLikes, "likeUserId", Long.class);
        Criteria criteria=new Criteria().where("toUserId").is(userId).and("userId").nin(likeUserId);
        TypedAggregation<RecommendUser> newAggregation = TypedAggregation.newAggregation(RecommendUser.class,
                Aggregation.match(criteria),//指定查询条件
                Aggregation.sample(counts)
        );
        //4、构造返回
        return mongoTemplate.aggregate(newAggregation, RecommendUser.class).getMappedResults();
    }
}
