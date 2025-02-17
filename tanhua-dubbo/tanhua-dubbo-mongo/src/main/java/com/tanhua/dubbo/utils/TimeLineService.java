package com.tanhua.dubbo.utils;

import com.tanhua.model.mongo.Friend;
import com.tanhua.model.mongo.MovementTimeLine;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TimeLineService {

    @Autowired
    private MongoTemplate mongoTemplate;
    public void saveTimeLine(Long userId, ObjectId movementId){
        Criteria criteria = new Criteria().where("userId").is(userId);
        Query query = new Query(criteria);
        List<Friend> friends = mongoTemplate.find(query, Friend.class);
        for (Friend friend : friends) {
            MovementTimeLine timeLine = new MovementTimeLine();
            timeLine.setMovementId(movementId);
            timeLine.setUserId(friend.getUserId());
            timeLine.setFriendId(friend.getFriendId());
            timeLine.setCreated(System.currentTimeMillis());
            mongoTemplate.save(timeLine);
        }
    }

}
