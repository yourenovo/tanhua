package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VisitorsApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.mongo.Visitors;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.VisitorsVo;
import com.tanhua.server.Interceptor.UserHolder;
import com.tanhua.server.exception.BusinessException;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovementService {
    @DubboReference
    private MovementApi movementApi;
    @Autowired
    private OssTemplate ossTemplate;
    @DubboReference
    private UserInfoApi userInfoApi;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @DubboReference
    private VisitorsApi visitorsApi;
    @Autowired
    private MqMessageService mqMessageService;

    public void publishMovement(Movement movement, MultipartFile[] imageContent) throws IOException {
        //判断movement是否为空
        if (movement.getTextContent().isEmpty()) {
            throw new BusinessException(ErrorResult.contentError());
        }
        //获取medis参数
        List<String> medias = new ArrayList<>();
        for (MultipartFile multipartFile : imageContent) {
            String upload = ossTemplate.upload(multipartFile.getOriginalFilename(), multipartFile.getInputStream());
            medias.add(upload);
        }
        movement.setMedias(medias);
        //获取用户id
        movement.setUserId(UserHolder.getUserId());
        String publishId = movementApi.publish(movement);
        mqMessageService.sendAudiService(publishId);
    }

    public PageResult findByUseId(Long userId, Integer page, Integer pageSize) {
        //查找movements
        PageResult pageResult = movementApi.findByUserId(userId, page, pageSize);
        //查找userInfo
        UserInfo userInfo = userInfoApi.findById(userId);

        //构建vo对象
        List<Movement> items = (List<Movement>) pageResult.getItems();
        //创建最后返回的vos
        List<MovementsVo> vos = new ArrayList<>();
        for (Movement item : items) {
            MovementsVo vo = MovementsVo.init(userInfo, item);
            vos.add(vo);
        }
        pageResult.setItems(vos);
        return pageResult;
    }

    public PageResult findFriendMovements(Integer page, Integer pageSize) {

        //查询list--movement
        Long userId = UserHolder.getUserId();
        List<Movement> movements = movementApi.findFriendMovements(userId, page, pageSize);
        if (CollUtil.isEmpty(movements)) {
            return new PageResult();
        }
        //查询list--userInfo
        List<MovementsVo> vos = getMovementsVos(movements);
        return new PageResult(page, pageSize, 0, vos);
    }

    private List<MovementsVo> getMovementsVos(List<Movement> movements) {
        List<Long> ids = CollUtil.getFieldValues(movements, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(ids, null);
        //构建返回值集合vos
        List<MovementsVo> vos = new ArrayList<>();
        for (Movement movement : movements) {
            UserInfo userInfo = map.get("userId");
            MovementsVo vo = MovementsVo.init(userInfo, movement);
            //修复点赞状态bug
            String key = Constants.MOVEMENTS_INTERACT_KEY + movement.getId().toHexString();
            String hasKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();
            String hasKey2 = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();
            if (redisTemplate.opsForHash().hasKey(key, hasKey)) {
                vo.setHasLiked(1);
            }
            if (redisTemplate.opsForHash().hasKey(key, hasKey2)) {
                vo.setHasLoved(1);
            }

            vos.add(vo);
        }
        return vos;
    }

    public PageResult findRecommendMovements(Integer page, Integer pageSize) {
        //从redis获取pid
        Long userId = UserHolder.getUserId();
        String redisKey = "MOVEMENTS_RECOMMEND_" + userId;
        String redisData = this.redisTemplate.opsForValue().get(redisKey);
        List<Movement> movements = Collections.emptyList();
        //如果pid不存在随机获取10条movement
        if (redisData.isEmpty()) {
            movements = movementApi.randomMovements(10);
        } else {
            //如果pid存在调用api获取指定推荐数据
            String[] split = redisData.split(",");
            if ((page - 1) * pageSize > split.length) {
                return new PageResult();
            } else {
                List<Long> pids = Arrays.stream(split).skip((page - 1) * pageSize).limit(pageSize)
                        .map(e -> Convert.toLong(e))
                        .collect(Collectors.toList());
                movements = movementApi.findRecommendMovements(pids);
            }

        }
        //构造vo对象
        List<MovementsVo> vos = getMovementsVos(movements);
        return new PageResult(page, pageSize, 0, vos);

    }

    public MovementsVo findById(String movementId) {
        Movement movement = movementApi.findById(movementId);
        if (movement == null) {
            return null;
        } else {
            UserInfo byId = userInfoApi.findById(movement.getUserId());
            return MovementsVo.init(byId, movement);
        }

    }

    public List<VisitorsVo> queryVisitorsList() {
        //获取date
        String key = Constants.VISITORS_USER;
        String hashKey = String.valueOf(UserHolder.getUserId());
        String value = (String) redisTemplate.opsForHash().get(key, hashKey);
        Long date = StringUtils.isEmpty(value) ? null : Long.valueOf(value);
        //查询访问列表
        List<Visitors> visitors = visitorsApi.queryMyVisitor(date, UserHolder.getUserId());
        if (CollUtil.isEmpty(visitors)) {
            return new ArrayList<>();
        }
        List<Long> userIds = CollUtil.getFieldValues(visitors, "visitorUserId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);

        //构造vos
        List<VisitorsVo> vos = new ArrayList<>();
        for (Visitors visitor : visitors) {
            UserInfo userInfo = map.get(visitor.getVisitorUserId());
            if (userInfo != null) {
                VisitorsVo vo = VisitorsVo.init(userInfo, visitor);
                vos.add(vo);
            }

        }

        return vos;
    }
}
