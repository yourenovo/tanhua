package com.tanhua.admin.listener;

import com.tanhua.autoconfig.template.AliyunGreenTemplate;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.model.mongo.Movement;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MovementListener {
    @Autowired
    private AliyunGreenTemplate aliyunGreenTemplate;
    @DubboReference
    private MovementApi movementApi;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    value = "tanhua.audit.queue",
                    durable = "true"
            ),
            exchange = @Exchange(
                    value = "tanhua.audit.exchange",
                    type = ExchangeTypes.TOPIC),
            key = {"audit.movement"})
    )
    public void listenCreate(String movementId) throws Exception {
        //查询动态判断状态
        try {
            Movement movement = movementApi.findById(movementId);
            int state = 0;
            if (movement == null || movement.getState() != 0) {
                Map<String, String> stringStringMap = aliyunGreenTemplate.greenTextScan(movement.getTextContent());
                Map<String, String> imageMap = aliyunGreenTemplate.imageScan(movement.getMedias());
                if (stringStringMap != null && imageMap != null) {
                    if ("block".equals(stringStringMap.get("suggestion")) || "block".equals(imageMap.get("suggestion"))){
                        state = 2;
                    }else if("pass".equals(stringStringMap.get("suggestion")) || "pass".equals(imageMap.get("suggestion"))) {
                        state = 1;
                    }
                }
            }
            //更新状态
            movementApi.update(movementId,state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
