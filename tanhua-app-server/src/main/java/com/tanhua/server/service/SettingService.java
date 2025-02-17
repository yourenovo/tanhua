package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.dubbo.api.BlackListApi;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.SettingsApi;
import com.tanhua.model.domain.Question;
import com.tanhua.model.domain.Settings;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.SettingsVo;
import com.tanhua.server.Interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SettingService {
    @DubboReference
    private QuestionApi questionApi;

    @DubboReference
    private SettingsApi settingsApi;


    @DubboReference
    private BlackListApi blackListApi;

    public SettingsVo settings() {
        SettingsVo settingsVo = new SettingsVo();
        settingsVo.setId(UserHolder.getUserId());
        settingsVo.setPhone(UserHolder.getMobile());
        Question question = questionApi.findByUseId(UserHolder.getUserId());
        String txt = question.getTxt();
        String s = txt != null ? txt : "你喜欢java吗";
        settingsVo.setStrangerQuestion(s);
        Settings settings = settingsApi.findByUserId(UserHolder.getUserId());
        if (settings != null) {
            settingsVo.setGonggaoNotification(settings.getGonggaoNotification());
            settingsVo.setLikeNotification(settings.getLikeNotification());
            settingsVo.setPinglunNotification(settings.getPinglunNotification());

        }
        return settingsVo;
    }

    public void saveQuestion(String context) {


        Question question = questionApi.findByUseId(UserHolder.getUserId());
        if(question==null){
            question=new Question();
            question.setUserId(UserHolder.getUserId());
            question.setTxt(context);
            questionApi.save(question);
        }else {
            question.setTxt(context);
            questionApi.update(question);
        }

    }

    public void save(Settings settings) {
        settingsApi.save(settings);
    }

    public void update(Settings settings) {
        settingsApi.update(settings);
    }

    public void saveSettings(Map map) {
        boolean likeNotification = (Boolean) map.get("likeNotification");
        boolean pinglunNotification = (Boolean) map.get("pinglunNotification");
        boolean gonggaoNotification = (Boolean)  map.get("gonggaoNotification");
        //1、获取当前用户id
        Long userId = UserHolder.getUserId();
        //2、根据用户id，查询用户的通知设置
        Settings settings = settingsApi.findByUserId(userId);
        //3、判断
        if(settings == null) {
            //保存
            settings = new Settings();
            settings.setUserId(userId);
            settings.setPinglunNotification(pinglunNotification);
            settings.setLikeNotification(likeNotification);
            settings.setGonggaoNotification(gonggaoNotification);
            settingsApi.save(settings);
        }else {
            settings.setPinglunNotification(pinglunNotification);
            settings.setLikeNotification(likeNotification);
            settings.setGonggaoNotification(gonggaoNotification);
            settingsApi.update(settings);
        }
    }

    public PageResult blacklist(int page, int size) {
        IPage<UserInfo>iPage=blackListApi.findByUserId(UserHolder.getUserId(),page,size);
        PageResult pageResult=new PageResult(page,size, (int) iPage.getTotal(),iPage.getRecords());
        return pageResult;
    }

    public void deleteBlackList(Long blackUserId) {
        blackListApi.deleteBlackList(UserHolder.getUserId(),blackUserId);

    }
}
