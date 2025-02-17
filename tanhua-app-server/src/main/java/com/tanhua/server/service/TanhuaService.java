package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.*;
import com.tanhua.model.domain.Question;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.dto.RecommendUserDto;
import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.model.mongo.Visitors;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.NearUserVo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.TodayBest;
import com.tanhua.server.Interceptor.UserHolder;
import com.tanhua.server.exception.BusinessException;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TanhuaService {
    @DubboReference
    private RecommendUserApi recommendUserApi;
    @DubboReference
    private UserInfoApi userInfoApi;
    @DubboReference
    private QuestionApi questionApi;
    @Autowired
    private HuanXinTemplate template;

    @Value("${tanhua.default.recommend.users}")
    private String recommendUser;
    @Autowired
    private RedisTemplate redisTemplate;
    @DubboReference
    private UserLikeApi userLikeApi;

    @DubboReference
    private UserLocationApi userLocationApi;

    @DubboReference
    private VisitorsApi visitorsApi;

    public TodayBest todayBest() {
        RecommendUser recommendUser = recommendUserApi.queryWithMaxScore(UserHolder.getUserId());
        if (recommendUser == null) {
            recommendUser = new RecommendUser();
            recommendUser.setUserId(1L);
            recommendUser.setScore(99d);
        }
        UserInfo userInfo = userInfoApi.findById(recommendUser.getUserId());
        return TodayBest.init(userInfo, recommendUser);

    }

    public PageResult recommendation(RecommendUserDto dto) {
        Long userId = UserHolder.getUserId();
        PageResult pageResult = recommendUserApi.queryRecommendUserList(dto.getPage(), dto.getPagesize(), userId);
        List<RecommendUser> items = (List<RecommendUser>) pageResult.getItems();
        if (items.isEmpty() || items.size() <= 0) {
            return pageResult;
        }
        //获取ids集合
        List<Long> ids = CollUtil.getFieldValues(items, "userId", Long.class);
        //获取dto的要求userInfo对象
        UserInfo userInfo = new UserInfo();
        userInfo.setAge(dto.getAge());
        userInfo.setGender(dto.getGender());
        //构建ids,userInfo的map集合,一次性查找全部userInfo
        Map<Long, UserInfo> map = userInfoApi.findByIds(ids, userInfo);
        //设置vo返回集合
        ArrayList<TodayBest> vos = new ArrayList<>();
        //循环items构建vo对象
        for (RecommendUser item : items) {
            UserInfo info = map.get(userId);
            if (info != null) {
                TodayBest vo = TodayBest.init(info, item);
                vos.add(vo);
            }
        }
        pageResult.setItems(vos);
        return pageResult;
    }

    public TodayBest personalInfo(Long userId) {

        //获取userInfo
        UserInfo userInfo = userInfoApi.findById(userId);
        //获取recommendUser
        RecommendUser recommendUser = recommendUserApi.queryByUserId(userId, UserHolder.getUserId());
        //调用api保存数据
        Visitors visitors=new Visitors();
        visitors.setUserId(userId);
        visitors.setVisitorUserId(UserHolder.getUserId());
        visitors.setScore(recommendUser.getScore());
        visitors.setDate(System.currentTimeMillis());
        visitors.setVisitDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        visitors.setFrom("首页");
        visitorsApi.save(visitors);
        return TodayBest.init(userInfo, recommendUser);

    }

    public String strangerQuestions(Long userId) {
        Question question = questionApi.findByUseId(userId);
        return question == null ? "你喜欢java编程吗？" : question.getTxt();
    }


    public void replyQuestions(String reply, Long userId) {
        Map map = new HashMap<>();
        //创建传输map对象，封装为json格式
        map.put("userId", UserHolder.getUserId());
        map.put("huanXinId", Constants.HX_USER_PREFIX + UserHolder.getUserId());
        map.put("nickName", userInfoApi.findById(UserHolder.getUserId()).getNickname());
        map.put("strangerQuestion", strangerQuestions(userId));
        map.put("reply", reply);

        //调用接口传输JSON数据
        String s = JSON.toJSONString(map);
        Boolean aBoolean = template.sendMsg(Constants.HX_USER_PREFIX + userId, s);
        if (!aBoolean) {
            throw new BusinessException(ErrorResult.error());
        }
    }

    public List<TodayBest> queryCardsList() {

        //查询list-recommend
        List<RecommendUser> recommendUsers = recommendUserApi.queryCardsList(UserHolder.getUserId(), 10);
        //判读是否为空设定默认值
        if (CollUtil.isEmpty(recommendUsers)) {
            String[] userIdS = recommendUser.split(",");
            recommendUsers = new ArrayList<>();
            for (String userId : userIdS) {
                RecommendUser recommendUser = new RecommendUser();
                recommendUser.setUserId(Convert.toLong(userId));
                recommendUser.setToUserId(UserHolder.getUserId());
                recommendUser.setScore(RandomUtil.randomDouble(60, 90));
                recommendUsers.add(recommendUser);
            }
        }
        //返回list-Todaybest
        List<Long> userIds = CollUtil.getFieldValues(recommendUsers, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        List<TodayBest> vos = new ArrayList<>();
        for (RecommendUser user : recommendUsers) {
            UserInfo userInfo = map.get(user.getUserId());
            TodayBest vo = TodayBest.init(userInfo, user);
            vos.add(vo);
        }
        return vos;
    }
   @Autowired
   MessagesService messagesService;
    public void likeUser(Long likeUserId) {
        //存入userLike表
        Boolean flag = userLikeApi.saveOrUpdate(UserHolder.getUserId(), likeUserId, true);
        if (!flag) {
            throw new BusinessException(ErrorResult.error());
        }
        //存入redis的set集合中
        redisTemplate.opsForSet().remove(Constants.USER_NOT_LIKE_KEY+UserHolder.getUserId(),likeUserId);
        redisTemplate.opsForSet().add(Constants.USER_LIKE_KEY+UserHolder.getUserId(),likeUserId);
        if(isLike(likeUserId,UserHolder.getUserId())){
                messagesService.contacts(likeUserId);
        }
    }
    public Boolean isLike(Long userId,Long likeUserId){
        return redisTemplate.opsForSet().isMember(Constants.USER_LIKE_KEY+userId,likeUserId);
    }
    public void notLikeUser(Long likeUserId) {
        //1、调用API，保存喜欢数据(保存到MongoDB中)
        Boolean save = userLikeApi.saveOrUpdate(UserHolder.getUserId(),likeUserId,false);
        if(!save) {
            //失败
            throw new BusinessException(ErrorResult.error());
        }
        //2、操作redis，写入喜欢的数据，删除不喜欢的数据 (喜欢的集合，不喜欢的集合)
        redisTemplate.opsForSet().add(Constants.USER_NOT_LIKE_KEY+UserHolder.getUserId(),likeUserId.toString());
        redisTemplate.opsForSet().remove(Constants.USER_LIKE_KEY+UserHolder.getUserId(),likeUserId.toString());
        //3、判断是否双向喜欢，删除好友(各位自行实现)
    }

    public List<NearUserVo> queryNearUser(String gender, String distance) {
        //查询list-userId
        List<Long>userIds= userLocationApi.queryNearUser(UserHolder.getUserId(), Long.valueOf(distance));
        UserInfo userInfo=new UserInfo();
        userInfo.setGender(gender);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds,userInfo);
        //构造nearUserVo
        List<NearUserVo>vos=new ArrayList<>();
        for (Long userId : userIds) {
            if(userId==UserHolder.getUserId()){
                continue;
            }
            UserInfo userInfo1 = map.get(userId);
            if(userInfo1!=null){
                NearUserVo vo = NearUserVo.init(userInfo1);
                vos.add(vo);
            }
        }
       return vos;
    }
}
