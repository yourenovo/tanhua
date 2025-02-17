package com.tanhua.admin.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VideoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ManagerService {
    @DubboReference
    private UserInfoApi userInfoApi;
    @DubboReference
    private VideoApi videoApi;
    @DubboReference
    private MovementApi movementApi;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public ResponseEntity findAllUsers(Integer page, Integer pagesize) {
        //1、调用API分页查询数据列表   Ipage<UserInfo>
        IPage<UserInfo> iPage = userInfoApi.findAll(page, pagesize);
        List<UserInfo> records = iPage.getRecords();
        for (UserInfo userInfo : records) {
            if (redisTemplate.hasKey("FREEZE_USER_" + userInfo.getId())) {
                userInfo.setUserStatus("2");
            }
        }
        //2、需要将Ipage转化为PageResult
        PageResult result = new PageResult(page, pagesize, (int) iPage.getTotal(), records);
        //3、构造返回值
        return ResponseEntity.ok(result);
    }

    public ResponseEntity findById(Long userId) {
        UserInfo info = userInfoApi.findById(userId);
        if (redisTemplate.opsForValue().get("FREEZE_USER_" + userId) != null) {
            info.setUserStatus(String.valueOf(2));
        }
        return ResponseEntity.ok(info);
    }

    public PageResult findAllVideos(Integer page, Integer pagesize, Long uid) {
        return videoApi.findAllVideos(page, pagesize, uid);
    }

    public PageResult findAllMovements(Integer page, Integer pagesize, Long uid, Integer state) {
        PageResult result = movementApi.findByUserId(uid, page, pagesize);
        List<Movement> items = (List<Movement>) result.getItems();
        List<MovementsVo> list = new ArrayList<>();
        for (Movement item : items) {
            UserInfo userInfo = userInfoApi.findById(item.getUserId());
            MovementsVo vo = MovementsVo.init(userInfo, item);
            list.add(vo);
        }
        //3、构造返回值
        result.setItems(list);
        return result;

    }

    public Map userUnfreeze(Map params) {
        String userId = (String) params.get("userId");
        String key = "FREEZE_USER_" + userId;
        Integer freezingTime = (Integer) params.get("freezingTime");
        int days = 0;
        if (freezingTime == 1) {
            days = 3;
        } else if (freezingTime == 2) {
            days = 7;
        } else if (freezingTime == 3) {
            days = -1;
        }
        String json = JSON.toJSONString(params);
        redisTemplate.opsForValue().set(key, json, days, TimeUnit.DAYS);
        Map retMap = new HashMap<>();
        retMap.put("message", "冻结成功");
        return retMap;
    }

    public Map userFreeze(Map params) {
        String userId = (String) params.get("userId");
        String key = "FREEZE_USER_" + userId;
        redisTemplate.delete(key);
        Map retMap = new HashMap<>();
        retMap.put("message", "解冻成功");
        return retMap;
    }
}
