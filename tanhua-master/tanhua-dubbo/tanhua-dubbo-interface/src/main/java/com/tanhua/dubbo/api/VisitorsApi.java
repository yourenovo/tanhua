package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Visitors;

import java.util.List;

public interface VisitorsApi {
    void save(Visitors visitors);

    List<Visitors> queryMyVisitor(Long date, Long userId);
}
