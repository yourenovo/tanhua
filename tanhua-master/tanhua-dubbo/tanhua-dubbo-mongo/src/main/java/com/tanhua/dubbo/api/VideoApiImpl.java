package com.tanhua.dubbo.api;

import com.tanhua.dubbo.utils.IdWorker;
import com.tanhua.model.mongo.FocusUser;
import com.tanhua.model.mongo.Video;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class VideoApiImpl implements VideoApi {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IdWorker idWorker;

    @Override
    public String save(Video video) {
        video.setVid(idWorker.getNextId("video"));
        video.setCreated(System.currentTimeMillis());
        mongoTemplate.save(video);
        return video.getId().toHexString();

    }

    @Override
    public List<Video> queryVideoByVids(List<Long> vids) {
        if (vids.isEmpty()) {
            return null;
        }
        Query query = new Query(new Criteria().where("vid").in(vids));
        return mongoTemplate.find(query, Video.class);
    }

    @Override
    public List<Video> queryVideoList(int page, Integer pageSize) {
        Query query = new Query().skip((page - 1) * pageSize).limit(pageSize).with(Sort.by(Sort.Order.desc("created")));
        return mongoTemplate.find(query, Video.class);
    }

    @Override
    public void saveFollowUser(FocusUser followUser) {
        followUser.setId(ObjectId.get());
        followUser.setCreated(System.currentTimeMillis());
        mongoTemplate.save(followUser);
    }

    @Override
    public void deleteFollowUser(Long userId, Long followUserId) {
        Query query = new Query(new Criteria().where("userId").is(userId).and("followUserId").is(followUserId));
        FocusUser one = mongoTemplate.findOne(query, FocusUser.class);
        if (one != null) {
            mongoTemplate.remove(query, FocusUser.class);
        } else {
            throw new RuntimeException("删除出错");
        }
    }

    @Override
    public PageResult findAllVideos(Integer page, Integer pageSize, Long userId) {
        Query query = new Query(new Criteria().where("userId").is(userId));
        long count = mongoTemplate.count(query, Video.class);
        query = query.skip((page - 1) * pageSize).limit(pageSize).with(Sort.by(Sort.Order.desc("created")));
        List<Video> videos = mongoTemplate.find(query, Video.class);

        return new PageResult(page, pageSize, (int) count, videos);

    }
}
