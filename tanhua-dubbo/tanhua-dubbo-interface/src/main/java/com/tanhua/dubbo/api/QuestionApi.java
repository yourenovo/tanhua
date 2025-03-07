package com.tanhua.dubbo.api;

import com.tanhua.model.domain.Question;

public interface QuestionApi {

    Question findByUseId(Long userId);

    void save(Question question);


    void update(Question question);
}