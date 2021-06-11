# [Mirai-AntiPorn-Plugin](https://github.com/gnuf0rce/Mirai-AntiPorn-Plugin)
> 基于 [Mirai Console](https://github.com/mamoe/mirai-console) 和 [百度AI内容审核](https://ai.baidu.com/ai-doc/ANTIPORN/) 自动禁言助手

[![Release](https://img.shields.io/github/v/release/gnuf0rce/Mirai-AntiPorn-Plugin)](https://github.com/gnuf0rce/Mirai-AntiPorn-Plugin/releases)
[![Release](https://img.shields.io/github/downloads/gnuf0rce/Mirai-AntiPorn-Plugin/total)](https://shields.io/category/downloads)

## 设置

使用前请根据 [内容审核平台-快速入门](https://ai.baidu.com/ai-doc/ANTIPORN/Wkhu9d5iy) 的步骤申请APP 
获取`APP_ID` `API_KEY` `SECRET_KEY`  
在安装插件，并启动`Mirai Console`后，`config/anti-porn`目录下会生成`ContentCensor.yml`  
对应修改文件中`APP_ID` `API_KEY` `SECRET_KEY`的值

机器人将会在作为群主或者管理员身份的情况下工作，即群消息内容审核（文本，图片）

## 安装

### 手动安装

1. 运行 [Mirai Console](https://github.com/mamoe/mirai-console) 生成`plugins`文件夹
1. 从 [Releases](https://github.com/cssxsh/Mirai-AntiPorn-Plugin/releases) 下载`jar`并将其放入`plugins`文件夹中

## TODO

- [ ] 配置禁言时间
- [ ] QPS延迟
- [ ] 视频内容审核
