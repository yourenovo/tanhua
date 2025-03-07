package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Friend;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class FriendApiImpl implements FriendApi {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public void save(Long userId, Long friendId) {
        Query query = new Query(new Criteria().where("userId").is(userId).and("friendId").is(friendId));
        boolean exists = mongoTemplate.exists(query, Friend.class);
        if (!exists) {
            Friend friend = new Friend();
            friend.setFriendId(friendId);
            friend.setUserId(userId);
            friend.setCreated(System.currentTimeMillis());
            mongoTemplate.save(friend);
        }

        Query query2 = Query.query(Criteria.where("userId").is(friendId).and("frinedId").is(userId));
        //2.1 判断好友关系是否存在
        if (!mongoTemplate.exists(query2, Friend.class)) {
            //2.2 如果不存在，保存
            Friend friend1 = new Friend();
            friend1.setUserId(friendId);
            friend1.setFriendId(userId);
            friend1.setCreated(System.currentTimeMillis());
            mongoTemplate.save(friend1);
        }
    }

    @Override
    public List<Friend> findByUserId(Long userId, Integer page, Integer pagesize) {
        Query query = new Query(new Criteria().where("userId").is(userId)).limit(pagesize).skip((page - 1) * pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        return mongoTemplate.find(query, Friend.class);

    }
}

