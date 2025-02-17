package com.tanhua.server.service;

import com.tanhua.autoconfig.template.AipFaceTemplate;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.exception.BusinessException;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class UserInfoService {
    @DubboReference
    private UserInfoApi userInfoApi;
    @Autowired
    private OssTemplate ossTemplate;
    @Autowired
    private AipFaceTemplate aipFaceTemplate;

    public void save(UserInfo userInfo){
        userInfoApi.save(userInfo);
    }

    public void update(UserInfo userInfo) {
        userInfoApi.update(userInfo);
    }

    public void updateHead(MultipartFile head, Long id) throws IOException {
        String url = head.getOriginalFilename();
        InputStream inputStream = head.getInputStream();
        String upload = ossTemplate.upload(url, inputStream);
        boolean detect = aipFaceTemplate.detect(upload);
        if(!detect){
            throw new BusinessException(ErrorResult.faceError());
        }else {
            UserInfo userInfo=new UserInfo();
            userInfo.setId(id);
            userInfo.setAvatar(url);
            userInfoApi.update(userInfo);
        }

    }

    public UserInfoVo findById(Long useId) {
         UserInfo userInfo=userInfoApi.findById(useId);
         UserInfoVo userInfoVo=new UserInfoVo();
        BeanUtils.copyProperties(userInfo,userInfoVo);
        if (userInfo.getAge()!=null){
            userInfoVo.setAge(userInfo.getAge().toString());
        }
         return userInfoVo ;
    }
}
