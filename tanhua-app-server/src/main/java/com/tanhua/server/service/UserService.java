package com.tanhua.server.service;

import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.autoconfig.template.SmsTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.commons.utils.JwtUtils;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.model.domain.User;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.server.exception.BusinessException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private SmsTemplate template;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @DubboReference
    private UserApi userApi;
    @Autowired
    HuanXinTemplate huanXinTemplate;
    @Autowired
    private UserFreezeService userFreezeService;
    @Autowired
    private MqMessageService mqMessageService;

    /**
     * 发送短信验证码
     *
     * @param phone
     */
    public void sendMsg(String phone) {
        if (userApi.findByMobile(phone) != null) {
            userFreezeService.checkUserStatus(1, userApi.findByMobile(phone).getId());
        }
        //1、随机生成6位数字
        //String code = RandomStringUtils.randomNumeric(6);
        String code = "123456";
        //2、调用template对象，发送手机短信
        //template.sendSms(phone,code);
        //3、将验证码存入到redis
        redisTemplate.opsForValue().set("CHECK_CODE_" + phone, code, Duration.ofMinutes(5));
    }


    /**
     * 验证登录
     *
     * @param phone
     * @param code
     */
    public Map loginVerification(String phone, String code) {
        //1、从redis中获取下发的验证码
        String redisCode = redisTemplate.opsForValue().get("CHECK_CODE_" + phone);
        //2、对验证码进行校验（验证码是否存在，是否和输入的验证码一致）
        if (StringUtils.isEmpty(redisCode) || !redisCode.equals(code)) {
            //验证码无效
            throw new BusinessException(ErrorResult.loginError());
        }
        //3、删除redis中的验证码
        redisTemplate.delete("CHECK_CODE_" + phone);
        //4、通过手机号码查询用户
        User user = userApi.findByMobile(phone);
        boolean isNew = false;
        String type = "0101";
        //5、如果用户不存在，创建用户保存到数据库中
        if (user == null) {
            type = "0102";
            user = new User();
            user.setMobile(phone);
            user.setPassword(DigestUtils.md5Hex("123456"));
            Long userId = userApi.save(user);
            user.setId(userId);
            isNew = true;

            //用户不存在注册到环信服务端
            String hxUser = "hx" + user.getId();
            Boolean flag = huanXinTemplate.createUser(hxUser, Constants.INIT_PASSWORD);
            if (flag) {
                user.setHxUser(hxUser);
                user.setPassword(Constants.INIT_PASSWORD);
                userApi.update(user);
            }

        }

        mqMessageService.sendLogService(user.getId(), type, "user", null);
        //6、通过JWT生成token(存入id和手机号码)
        Map tokenMap = new HashMap();
        tokenMap.put("id", user.getId());
        tokenMap.put("mobile", phone);
        String token = JwtUtils.getToken(tokenMap);
        //7、构造返回值
        Map retMap = new HashMap();
        retMap.put("token", token);
        retMap.put("isNew", isNew);

        return retMap;
    }
}
