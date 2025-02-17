package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Visitors;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class VisitorsApiImpl implements VisitorsApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void save(Visitors visitors) {
        Query query = new Query(new Criteria().where("userId").is(visitors.getUserId())
                .and("visitorUserId").is(visitors.getVisitorUserId())
                .and("visitDate").is(visitors.getDate()));
        if (!mongoTemplate.exists(query, Visitors.class)) {
            //保存
            mongoTemplate.save(visitors);
        }

    }

    @Override
    public List<Visitors> queryMyVisitor(Long date, Long userId) {
        //存在date比较date
        Criteria criteria = new Criteria().where("userId").is(userId);
        if (date != null) {
            criteria.and("date").gt(date);
        }

        //不存在获取desc时间五条
        Query query = new Query(criteria).limit(5).with(Sort.by(Sort.Order.desc("date")));
        List<Visitors> visitors = mongoTemplate.find(query, Visitors.class);
        return visitors;
    }
}
