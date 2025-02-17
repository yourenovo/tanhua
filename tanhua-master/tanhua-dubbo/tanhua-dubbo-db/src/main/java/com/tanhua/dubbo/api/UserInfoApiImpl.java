package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.dubbo.mappers.UserInfoMapper;
import com.tanhua.model.domain.UserInfo;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class UserInfoApiImpl implements UserInfoApi {
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public void save(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);
    }

    @Override
    public void update(UserInfo userInfo) {
        userInfoMapper.updateById(userInfo);
    }

    @Override
    public UserInfo findById(Long useId) {
        return userInfoMapper.selectById(useId);
    }

    @Override
    public Map<Long, UserInfo> findByIds(List<Long> ids, UserInfo userInfo) {
        //利用qw查找list集合
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.in(ids);
        if (userInfo.getAge() != null) {
            queryWrapper.lt("age", userInfo.getAge());
        }
        if (!userInfo.getGender().isEmpty()) {
            queryWrapper.eq("gender", userInfo.getGender());
        } if (!userInfo.getNickname().isEmpty()) {
            queryWrapper.like("nickName", userInfo.getNickname());
        }
        List<UserInfo> list = userInfoMapper.selectList(queryWrapper);
        //根据list集合构建map对象
        Map<Long, UserInfo> map = CollUtil.fieldValueMap(list, "id");
        return map;
    }

    @Override
    public IPage<UserInfo> findAll(Integer page, Integer pagesize) {
        return userInfoMapper.selectPage(new Page<>(page,pagesize),null);
    }


}
