# tanhua

#### 介绍

校园社交平台，实现朋友圈点赞，视频存储，热点推荐，个性化交友功能

#### 软件架构

软件架构说明

校园交友平台（后端开发）

技术栈：SpringBoot+MyBatisPlus+Redis+Dubbo+MongDB+Elasticsearch +FastDFS+SpringCloud
项目描述：
1.基于百度阿里第三方服务，完成人脸识别,短信发送业务，配合 token 实现用户鉴权

2.使用 nacos 作为微服务注册中心配置文件，基于 dubbo 实现微服务之间远程调用

3.使用 Mongo 存储用户热数据，以保证用户热数据高扩展和高性能指标

4.基于 FastDFS 作为静态资源存储器，在其上实现热静态资源缓存、淘汰等功能

5.基于 SpringCloud-GateWay 网关服务，实现服务注册中的 API 请求路由，以及控制流速控制和熔断处理

6.使用 rabbitmq 第三方文章审核服务，异步实现后台动态审核，发送日志数据


