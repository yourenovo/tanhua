package com.itheima.test;

import com.tanhua.dubbo.api.RecommendUserApi;
import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.server.AppServerApplication;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class MongoTest {
    @DubboReference
    private RecommendUserApi recommendUserApi;
    @Test
    public void test1(){
        RecommendUser recommendUser = recommendUserApi.queryWithMaxScore(106L);
        System.out.println(recommendUser);
    }
}
