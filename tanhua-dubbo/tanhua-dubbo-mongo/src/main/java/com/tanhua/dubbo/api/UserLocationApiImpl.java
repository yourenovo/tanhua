package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.model.mongo.UserLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserLocationApiImpl implements UserLocationApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Boolean updateLocation(Long userId, Double longitude, Double latitude, String address) {
        try {
            UserLocation one = mongoTemplate.findOne(new Query(new Criteria().where("userId").is(userId)), UserLocation.class);
            if (one == null) {
                //新增操作
                UserLocation userLocation = new UserLocation();
                userLocation.setUserId(userId);
                userLocation.setLocation(new GeoJsonPoint(longitude, latitude));
                userLocation.setUpdated(System.currentTimeMillis());
                userLocation.setLastUpdated(System.currentTimeMillis());
                userLocation.setCreated(System.currentTimeMillis());
                userLocation.setAddress(address);
            } else {
                //更新操作
                Update update = Update.update("location", new GeoJsonPoint(longitude, latitude))
                        .set("lastUpdated", one.getUpdated())
                        .set("updated", System.currentTimeMillis());
                mongoTemplate.updateFirst(Query.query(new Criteria().where("userId").is(userId)), update, UserLocation.class);

            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Long> queryNearUser(Long userId, Long metre) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        UserLocation location = mongoTemplate.findOne(query, UserLocation.class);
        if (location == null) {
            return null;
        }
        //2、已当前用户位置绘制原点
        GeoJsonPoint point = location.getLocation();
        //3、绘制半径
        Distance distance = new Distance(metre / 1000, Metrics.KILOMETERS);
        //4、绘制圆形
        Circle circle = new Circle(point, distance);
        //5、查询
        Query locationQuery = Query.query(Criteria.where("location").withinSphere(circle));
        List<UserLocation> list = mongoTemplate.find(locationQuery, UserLocation.class);
        return CollUtil.getFieldValues(list, "userId", Long.class);
    }
}

