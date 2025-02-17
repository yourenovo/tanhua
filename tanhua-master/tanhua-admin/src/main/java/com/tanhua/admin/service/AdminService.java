package com.tanhua.admin.service;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.admin.exception.BusinessException;
import com.tanhua.admin.interceptor.AdminHolder;
import com.tanhua.admin.mapper.AdminMapper;
import com.tanhua.commons.utils.Constants;
import com.tanhua.commons.utils.JwtUtils;
import com.tanhua.model.domain.Admin;
import com.tanhua.model.vo.AdminVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public Map login(Map map) {
        //获取对应信息
        String username = (String) map.get("username");
        String password = (String) map.get("password");
        String verificationCode = (String) map.get("verificationCode");
        String uuid = (String) map.get("uuid");
        //从redis取出判断验证码是否正确
        String value = redisTemplate.opsForValue().get(Constants.CAP_CODE + uuid);
        if (value.isEmpty() || !verificationCode.equals(value)) {
            throw new BusinessException("验证码错误");
        }
        redisTemplate.delete(Constants.CAP_CODE + uuid);
        //判断密码是否正确
        password = SecureUtil.md5(password);
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<Admin>().eq("username", username);
        Admin admin = adminMapper.selectOne(queryWrapper);
        if (admin == null || !password.equals(admin.getPassword())) {
            throw new BusinessException("密码错误");
        }
        Map tokenMap = new HashMap<>();
        tokenMap.put("username", username);
        tokenMap.put("id", admin.getId());
        String token = JwtUtils.getToken(tokenMap);
        //返回token
        Map map1 = new HashMap<>();
        map1.put("token", token);
        return map1;
    }

    public AdminVo profile() {
        Long id = AdminHolder.getUserId();
        Admin admin = adminMapper.selectById(id);
        return AdminVo.init(admin);
    }
}
