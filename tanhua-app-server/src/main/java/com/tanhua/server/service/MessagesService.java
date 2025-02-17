package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.FriendApi;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.model.domain.User;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.Friend;
import com.tanhua.model.vo.ContactVo;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.Interceptor.UserHolder;
import com.tanhua.server.exception.BusinessException;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MessagesService {
    @DubboReference
    private UserInfoApi userInfoApi;
    @DubboReference
    private UserApi userApi;
    @DubboReference
    private FriendApi friendApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    public UserInfoVo findUserInfoByHuanxin(String huanxinId) {
        User user = userApi.finByHxId(huanxinId);
        UserInfo userInfo = userInfoApi.findById(user.getId());
        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, vo);
        if (userInfo.getAge() != null) {
            vo.setAge(userInfo.getAge().toString());
        }
        return vo;

    }

    public void contacts(Long friendId) {

        Boolean aBoolean = huanXinTemplate.addContact(Constants.HX_USER_PREFIX + UserHolder.getUserId(), Constants.HX_USER_PREFIX + friendId);
        if (!aBoolean) {
            throw new BusinessException(ErrorResult.error());
        }
        friendApi.save(UserHolder.getUserId(), friendId);
    }

    public PageResult findFriends(Integer page, Integer pagesize, String keyword) {

        //查询list-friend
        List<Friend> friends = friendApi.findByUserId(UserHolder.getUserId(), page, pagesize);
        if (CollUtil.isEmpty(friends)) {
            return new PageResult();
        }

        //得到list-friendId
        List<Long> ids = CollUtil.getFieldValues(friends, "friendId", Long.class);

        //查询map-userId-userInfo
        UserInfo userInfo = new UserInfo();
        userInfo.setNickname(keyword);
        Map<Long, UserInfo> map = userInfoApi.findByIds(ids, userInfo);
        //构造list-vo
        List<ContactVo> vos = new ArrayList<>();
        for (Friend friend : friends) {

            Long friendId = friend.getFriendId();
            UserInfo userInfo1 = map.get(friendId);
            if (userInfo1 != null) {
                ContactVo vo = ContactVo.init(userInfo1);
                vos.add(vo);
            }

        }
        return new PageResult(page, pagesize, 0, vos);

    }
}
