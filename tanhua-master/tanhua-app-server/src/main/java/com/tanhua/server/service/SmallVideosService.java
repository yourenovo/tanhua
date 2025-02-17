package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.PageUtil;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VideoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.FocusUser;
import com.tanhua.model.mongo.Video;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.VideoVo;
import com.tanhua.server.Interceptor.UserHolder;
import com.tanhua.server.exception.BusinessException;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SmallVideosService {
    @DubboReference
    private VideoApi videoApi;
    @Autowired
    private OssTemplate ossTemplate;
    @Autowired
    private FastFileStorageClient client;
    @Autowired
    private FdfsWebServer webServer;
    @Autowired
    RedisTemplate<String, String> redisTemplate;
    @DubboReference
    private UserInfoApi userInfoApi;

    @CacheEvict(value="videoList",allEntries = true)
    public void saveVideos(MultipartFile videoThumbnail, MultipartFile videoFile) throws IOException {
        //获取视频上传后url
        if (videoFile.isEmpty() || videoThumbnail.isEmpty()) {
            throw new BusinessException(ErrorResult.error());
        }

        String fileName = videoFile.getOriginalFilename();
        fileName = fileName.substring(fileName.lastIndexOf(".") + 1);
        StorePath storePath = client.uploadFile(videoFile.getInputStream(), videoFile.getSize(), fileName, null);
        String videoUrl = storePath + webServer.getWebServerUrl();

        //获取封面上传url
        String imageUrl = ossTemplate.upload(videoThumbnail.getOriginalFilename(), videoThumbnail.getInputStream());

        //封装video对象保存
        Video video = new Video();
        video.setText("默认文本");
        video.setPicUrl(imageUrl);
        video.setVideoUrl(videoUrl);
        video.setUserId(UserHolder.getUserId());
        String videoId = videoApi.save(video);
        if (videoId.isEmpty()) {
            throw new BusinessException(ErrorResult.error());
        }
    }
    @Cacheable(value="videoList",key="#page + '_' +  #pagesize")
    public PageResult queryVideoList(Integer page, Integer pageSize) {
        //redis查询
        String redisKey = Constants.VIDEOS_RECOMMEND + UserHolder.getUserId();
        String redisValue = redisTemplate.opsForValue().get(redisKey);
        List<Video> videoList = new ArrayList<>();
        Integer redisPage = null;
        if (!redisValue.isEmpty()) {
            //如果pid存在调用api获取指定推荐数据
            String[] split = redisValue.split(",");
            if ((page - 1) * pageSize > split.length) {
                return new PageResult();
            } else {
                List<Long> vids = Arrays.stream(split).skip((page - 1) * pageSize).limit(pageSize)
                        .map(e -> Convert.toLong(e))
                        .collect(Collectors.toList());
                videoList = videoApi.queryVideoByVids(vids);
            }
            redisPage = PageUtil.totalPage(split.length, pageSize);
        }
        //调用api查询
        if (videoList.isEmpty()) {
            videoList = videoApi.queryVideoList(page - redisPage, pageSize);
        }
        //构造vo对象
        List<Long> ids = CollUtil.getFieldValues(videoList, "userId", Long.class);
        List<VideoVo> vos = new ArrayList<>();
        Map<Long, UserInfo> map = userInfoApi.findByIds(ids, null);
        for (Video video : videoList) {
            UserInfo userInfo = map.get(video.getUserId());
            if (userInfo != null) {
                VideoVo vo = VideoVo.init(userInfo, video);
                vos.add(vo);
            }
        }
        return new PageResult(page,pageSize,0,vos);
    }

    public void userFocus(Long followUserId) {
        //1、创建FollowUser对象，并设置属性
        FocusUser followUser = new FocusUser();
        followUser.setUserId(UserHolder.getUserId());
        followUser.setFollowUserId(followUserId);
        //2、调用API保存
        videoApi.saveFollowUser(followUser);
        //3、将关注记录存入redis中
        String key = Constants.FOCUS_USER_KEY + UserHolder.getUserId();
        String hashKey = String.valueOf(followUserId);
        redisTemplate.opsForHash().put(key,hashKey,"1");
    }

    //取消关注视频作者
    public void userUnFocus(Long followUserId) {
        //1、调用API删除关注数据
        videoApi.deleteFollowUser(UserHolder.getUserId(),followUserId);
        //2、删除redis中关注记录
        String key = Constants.FOCUS_USER_KEY + UserHolder.getUserId();
        String hashKey = String.valueOf(followUserId);
        redisTemplate.opsForHash().delete(key,hashKey);
    }
}
