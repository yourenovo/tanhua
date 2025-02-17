package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.CommentApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.vo.CommentVo;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.PageResult;
import com.tanhua.server.Interceptor.UserHolder;
import com.tanhua.server.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CommentService {
    @DubboReference
    CommentApi commentApi;
    @DubboReference
    UserInfoApi userInfoApi;
    @Autowired
    RedisTemplate<String, String> redisTemplate;

    public void saveComments(String movementId, String comment) {
        Comment comment1 = new Comment();
        comment1.setUserId(UserHolder.getUserId());
        comment1.setCommentType(CommentType.COMMENT.getType());
        comment1.setContent(comment);
        comment1.setPublishId(new ObjectId(movementId));
        comment1.setCreated(System.currentTimeMillis());
        Integer count = commentApi.save(comment1);
        log.info("count:" + count);
    }

    public PageResult findComments(String movementId, Integer page, Integer pagesize) {
        //查询comment-list
        List<Comment> comments = commentApi.findComments(page, pagesize, CommentType.COMMENT, movementId);
        if (CollUtil.isEmpty(comments)) {
            return new PageResult();
        }
        //查询userinfo-list
        List<Long> ids = CollUtil.getFieldValues(comments, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(ids, null);
        //封装成commentvo
        List<CommentVo> vos = new ArrayList<>();
        for (Comment comment : comments) {
            UserInfo userInfo = map.get("userId");
            CommentVo init = CommentVo.init(userInfo, comment);
            vos.add(init);
        }
        //返回pageresult参数
        return new PageResult(page, pagesize, 0, vos);

    }

    public Integer likeComment(String movementId) {

        Boolean hasComment = commentApi.hasComment(movementId, UserHolder.getUserId(), CommentType.LIKE);
        if (hasComment) {
            throw new BusinessException(ErrorResult.likeError());
        }

        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LIKE.getType());
        comment.setUserId(UserHolder.getUserId());
        comment.setCreated(System.currentTimeMillis());
        Integer count = commentApi.save(comment);
        //把用户点赞状态存储到redis中
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hasKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();
        redisTemplate.opsForHash().put(key, hasKey, 1);
        return count;
    }

    public Integer dislikeComment(String movementId) {
        //1、调用API查询用户是否已点赞
        Boolean hasComment = commentApi.hasComment(movementId,UserHolder.getUserId(),CommentType.LIKE);
        //2、如果未点赞，抛出异常
        if(!hasComment) {
            throw new BusinessException(ErrorResult.disLikeError());
        }
        //3、调用API，删除数据，返回点赞数量
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LIKE.getType());
        comment.setUserId(UserHolder.getUserId());
        Integer count = commentApi.delete(comment);
        //4、拼接redis的key，删除点赞状态
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();
        redisTemplate.opsForHash().delete(key,hashKey);
        return count;
    }

    public Integer loveComment(String movementId) {
        //1、调用API查询用户是否已点赞
        Boolean hasComment = commentApi.hasComment(movementId,UserHolder.getUserId(),CommentType.LOVE);
        //2、如果已经喜欢，抛出异常
        if(hasComment) {
            throw  new BusinessException(ErrorResult.loveError());
        }
        //3、调用API保存数据到Mongodb
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LOVE.getType());
        comment.setUserId(UserHolder.getUserId());
        comment.setCreated(System.currentTimeMillis());
        Integer count = commentApi.save(comment);
        //4、拼接redis的key，将用户的点赞状态存入redis
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();
        redisTemplate.opsForHash().put(key,hashKey,"1");
        return count;
    }

    //取消喜欢
    public Integer disloveComment(String movementId) {
        //1、调用API查询用户是否已点赞
        Boolean hasComment = commentApi.hasComment(movementId,UserHolder.getUserId(),CommentType.LOVE);
        //2、如果未点赞，抛出异常
        if(!hasComment) {
            throw new BusinessException(ErrorResult.disloveError());
        }
        //3、调用API，删除数据，返回点赞数量
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LOVE.getType());
        comment.setUserId(UserHolder.getUserId());
        Integer count = commentApi.delete(comment);
        //4、拼接redis的key，删除点赞状态
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();
        redisTemplate.opsForHash().delete(key,hashKey);
        return count;
    }
}
