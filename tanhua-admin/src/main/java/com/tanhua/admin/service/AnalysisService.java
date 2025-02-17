package com.tanhua.admin.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.admin.mapper.AnalysisMapper;
import com.tanhua.admin.mapper.LogMapper;
import com.tanhua.model.domain.Analysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class AnalysisService {
    @Autowired
    private LogMapper logMapper;
    @Autowired
    private AnalysisMapper analysisMapper;

    public void analysis() throws ParseException {
        //查询log表
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String yesterday = DateUtil.yesterday().toString("yyyy-MM-dd");
        Integer logTime = logMapper.queryByLogTime(today);
        Integer queryByTypeAndLogTime2 = logMapper.queryByTypeAndLogTime("0102", today);
        Integer queryByTypeAndLogTime1 = logMapper.queryByTypeAndLogTime("0101", today);
        Integer queryNumRetention1d = logMapper.queryNumRetention1d(today, yesterday);
        QueryWrapper<Analysis> queryWrapper = new QueryWrapper<Analysis>();
        Date parse = new SimpleDateFormat("yyyy-MM-dd").parse(today);
        queryWrapper.eq("record_date", parse);
        Analysis analysis = analysisMapper.selectOne(queryWrapper);

        if (analysis != null) {
            //存在更新
            analysis.setNumRegistered(queryByTypeAndLogTime2);
            analysis.setNumLogin(queryByTypeAndLogTime1);
            analysis.setNumActive(logTime);
            analysis.setNumRetention1d(queryNumRetention1d);
            analysisMapper.updateById(analysis);
        } else {
            //不存在保存
            analysis = new Analysis();
            analysis.setNumRegistered(queryByTypeAndLogTime2);
            analysis.setNumLogin(queryByTypeAndLogTime1);
            analysis.setNumActive(logTime);
            analysis.setNumRetention1d(queryNumRetention1d);
            analysis.setCreated(new Date());
            analysisMapper.insert(analysis);
        }
    }

    public Long queryActiveUserCount(DateTime today, int offset) {
        return this.queryUserCount(today, offset, "num_active");
    }

    /**
     * 查询注册用户的数量
     */
    public Long queryRegisterUserCount(DateTime today, int offset) {
        return this.queryUserCount(today, offset, "num_registered");
    }

    /**
     * 查询登录用户的数量
     */
    public Long queryLoginUserCount(DateTime today, int offset) {
        return this.queryUserCount(today, offset, "num_login");
    }

    private Long queryUserCount(DateTime today, int offset, String num_login) {
        return null;
    }


    private Long queryAnalysisCount(String column, String today, String offset) {
        return analysisMapper.sumAnalysisData(column, today, offset);
    }
}
