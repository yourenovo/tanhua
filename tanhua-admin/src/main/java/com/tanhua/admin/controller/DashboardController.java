package com.tanhua.admin.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.tanhua.admin.service.AnalysisService;
import com.tanhua.model.vo.AnalysisSummaryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private AnalysisService analysisService;

    /**
     * 概要统计信息
     */
    @GetMapping("/dashboard/summary")
    public AnalysisSummaryVo getSummary() {

        AnalysisSummaryVo analysisSummaryVo = new AnalysisSummaryVo();

        DateTime dateTime = DateUtil.parseDate("2020-09-08");

        //累计用户数
        analysisSummaryVo.setCumulativeUsers(Long.valueOf(1000));

        //过去30天活跃用户
        analysisSummaryVo.setActivePassMonth(this.analysisService.queryActiveUserCount(dateTime, -30));

        //过去7天活跃用户
        analysisSummaryVo.setActivePassWeek(this.analysisService.queryActiveUserCount(dateTime, -7));

        //今日活跃用户
        analysisSummaryVo.setActiveUsersToday(this.analysisService.queryActiveUserCount(dateTime, 0));


        //今日新增用户
        analysisSummaryVo.setNewUsersToday(this.analysisService.queryRegisterUserCount(dateTime, 0));

        //今日新增用户涨跌率，单位百分数，正数为涨，负数为跌
        analysisSummaryVo.setNewUsersTodayRate(computeRate(
                analysisSummaryVo.getNewUsersToday(),
                this.analysisService.queryRegisterUserCount(dateTime, -1)
        ));

        //今日登录次数
        analysisSummaryVo.setLoginTimesToday(this.analysisService.queryLoginUserCount(dateTime, 0));

        //今日登录次数涨跌率，单位百分数，正数为涨，负数为跌
        analysisSummaryVo.setLoginTimesTodayRate(computeRate(
                analysisSummaryVo.getLoginTimesToday(),
                this.analysisService.queryLoginUserCount(dateTime, -1)
        ));


        return analysisSummaryVo;

    }

    private static BigDecimal computeRate(Long current, Long last) {
        BigDecimal result;
        if (last == 0) {
            // 当上一期计数为零时，此时环比增长为倍数增长
            result = new BigDecimal((current - last) * 100);
        } else {
            result = BigDecimal.valueOf((current - last) * 100).divide(BigDecimal.valueOf(last), 2, BigDecimal.ROUND_HALF_DOWN);
        }
        return result;
    }
    
    private static String offsetDay(Date date, int offSet) {
        return DateUtil.offsetDay(date,offSet).toDateStr();
    }
}