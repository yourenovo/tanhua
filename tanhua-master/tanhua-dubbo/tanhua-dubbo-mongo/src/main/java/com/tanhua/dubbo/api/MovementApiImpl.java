package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.dubbo.utils.IdWorker;
import com.tanhua.dubbo.utils.TimeLineService;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.mongo.MovementTimeLine;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@DubboService
public class MovementApiImpl implements MovementApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IdWorker idWorker;
    @Autowired
    private TimeLineService timeLineService;

    @Override
    public String publish(Movement movement) {
        try {
            movement.setCreated(System.currentTimeMillis());
            movement.setPid(idWorker.getNextId("movement"));
            mongoTemplate.save(movement);
            //储存好友表单内容
//            Criteria criteria = new Criteria().where("userId").is(movement.getUserId());
//            Query query = new Query(criteria);
//            List<Friend> friends = mongoTemplate.find(query, Friend.class);
//            for (Friend friend : friends) {
//                MovementTimeLine timeLine = new MovementTimeLine();
//                timeLine.setMovementId(movement.getId());
//                timeLine.setUserId(friend.getUserId());
//                timeLine.setFriendId(friend.getFriendId());
//                timeLine.setCreated(System.currentTimeMillis());
//                mongoTemplate.save(timeLine);
//            }
            timeLineService.saveTimeLine(movement.getUserId(), movement.getId());

        } catch (Exception e) {
            //忽略事务处理
            e.printStackTrace();
        }
        return movement.getId().toHexString();
    }

    @Override
    public PageResult findByUserId(Long userId, Integer page, Integer pageSize) {
        Criteria criteria = new Criteria().where("userId").is(userId).and("state").is(1);
        Query query = new Query(criteria).skip((page - 1) * pageSize).limit(pageSize).
                with(Sort.by(Sort.Order.desc("created")));
        List<Movement> movements = mongoTemplate.find(query, Movement.class);
        return new PageResult(page, pageSize, 0, movements);
    }

    @Override
    public List<Movement> findFriendMovements(Long userId, Integer page, Integer pageSize) {
        Query query = new Query(new Criteria().where("friendId").in(userId)).
                with(Sort.by(
                        Sort.Order.desc("created")
                )).skip((page - 1) * pageSize).limit(pageSize);
        List<MovementTimeLine> movementTimeLines = mongoTemplate.find(query, MovementTimeLine.class);
        List<Object> movementId = CollUtil.getFieldValues(movementTimeLines, "movementId", Object.class);
        Query query1 = new Query(new Criteria().where("id").in(movementId).and("state").is(1));

        return mongoTemplate.find(query1, Movement.class);
    }

    @Override
    public List<Movement> randomMovements(int i) {
        TypedAggregation aggregation = Aggregation.newAggregation(Movement.class,
                Aggregation.sample(i));

        AggregationResults<Movement> movements = mongoTemplate.aggregate(aggregation, Movement.class);

        return movements.getMappedResults();
    }

    @Override
    public List<Movement> findRecommendMovements(List<Long> pids) {
        Query query = new Query(new Criteria().where("pid").in(pids));
        return mongoTemplate.find(query, Movement.class);

    }

    @Override
    public Movement findById(String movementId) {
        Movement movement = mongoTemplate.findById(movementId, Movement.class);
        return movement;
    }

    @Override
    public void update(String movementId, int state) {
        Query query = new Query(new Criteria().where("id").is(movementId));
        Update update = new Update().set("state", state);
        mongoTemplate.updateFirst(query, update, Movement.class);
    }
}