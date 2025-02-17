package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.PageResult;

import java.util.List;

public interface MovementApi {

    String publish(Movement movement);

    PageResult findByUserId(Long userId, Integer page, Integer pageSize);

    List<Movement> findFriendMovements(Long userId, Integer page, Integer pageSize);

    List<Movement> randomMovements(int i);

    List<Movement> findRecommendMovements(List<Long> pids);

    Movement findById(String movementId);

    void update(String movementId, int state);
}