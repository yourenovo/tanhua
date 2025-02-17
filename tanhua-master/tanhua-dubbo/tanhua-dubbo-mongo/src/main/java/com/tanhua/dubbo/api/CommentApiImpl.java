package com.tanhua.dubbo.api;

import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.mongo.Movement;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@DubboService
public class CommentApiImpl implements CommentApi {
    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Integer save(Comment comment1) {
        //保存评论到comment表
        Movement movement = mongoTemplate.findById(comment1.getPublishId(), Movement.class);
        if (movement != null) {
            comment1.setPublishUserId(movement.getUserId());
        }
        mongoTemplate.save(comment1);
        Query query = new Query(new Criteria().where("id").is(comment1.getPublishId()));
        Update update = new Update();
        if (comment1.getCommentType() == CommentType.COMMENT.getType()) {
            update.inc("commentCount", 1);
        } else if (comment1.getCommentType() == CommentType.LOVE.getType()) {
            update.inc("loveCount", 1);
        } else {
            update.inc("likeCount", 1);
        }
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);
        //更新movement表对应字段
        Movement andModify = mongoTemplate.findAndModify(query, update, options, Movement.class);
        return andModify.statisCount(comment1.getCommentType());
    }

    @Override
    public List<Comment> findComments(Integer page, Integer pagesize, CommentType comment, String movementId) {
        Query query = new Query(new Criteria().where("published").is(new ObjectId(movementId)).and("comment").is(comment.getType()))
                .skip((page - 1) * pagesize)
                .limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        return mongoTemplate.find(query, Comment.class);
    }

    public Boolean hasComment(String movementId, Long userId, CommentType commentType) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("publishId").is(new ObjectId(movementId))
                .and("commentType").is(commentType.getType());
        Query query = Query.query(criteria);
        return mongoTemplate.exists(query, Comment.class); //判断数据是否存在
    }

    @Override
    public Integer delete(Comment comment) {
        //删除数据
        Criteria criteria = Criteria.where("userId").is(comment.getUserId())
                .and("publishId").is(comment.getPublishId())
                .and("commentType").is(comment.getCommentType());
        Query query = Query.query(criteria);
        mongoTemplate.remove(query, Comment.class);
        //修改表中数据
        Query query1 = new Query(new Criteria().where("id").is(comment.getPublishId()));
        Update update = new Update();
        if (comment.getCommentType() == CommentType.COMMENT.getType()) {
            update.inc("commentCount", -1);
        } else if (comment.getCommentType() == CommentType.LOVE.getType()) {
            update.inc("loveCount", -1);
        } else {
            update.inc("likeCount", -1);
        }
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);
        //更新movement表对应字段
        Movement andModify = mongoTemplate.findAndModify(query1, update, options, Movement.class);
        return andModify.statisCount(comment.getCommentType());
    }
}
