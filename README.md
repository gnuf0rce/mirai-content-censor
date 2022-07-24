# [Mirai Content Censor](https://github.com/gnuf0rce/mirai-content-censor)

> 基于 [Mirai Console](https://github.com/mamoe/mirai-console) 和 [百度AI内容审核](https://ai.baidu.com/ai-doc/ANTIPORN/) 的自动禁言助手

[![Release](https://img.shields.io/github/v/release/gnuf0rce/mirai-content-censor)](https://github.com/gnuf0rce/mirai-content-censor/releases)
[![Downloads](https://img.shields.io/github/downloads/gnuf0rce/mirai-content-censor/total)](https://shields.io/category/downloads)
[![MiraiForum](https://img.shields.io/badge/post-on%20MiraiForum-yellow)](https://mirai.mamoe.net/topic/293)

同时安装有 [Mirai Administrator](https://github.com/cssxsh/mirai-administrator) 时，将会桥接到服务接口

## 设置

### 百度AI

使用前请根据 <https://ai.baidu.com/ai-doc/ANTIPORN/Wkhu9d5iy> 的步骤申请APP 获取`APP_ID` `API_KEY` `SECRET_KEY`  
在安装插件，并启动`Mirai Console`后，`config/io.github.gnuf0rce.content-censor`目录下会生成`ContentCensor.yml`  
对应修改文件中`APP_ID` `API_KEY` `SECRET_KEY`的值

机器人将会在作为群主或者管理员身份的情况下工作，即群消息内容审核（文本，图片，语音）  
机器人的审核规则请到 <https://ai.baidu.com/censoring#/strategylist> 修改策略

### 审核内容

位于 `config/io.github.gnuf0rce.content-censor/ContentCensor.yml`

1. `mute` 禁言时间，单位秒，默认1分钟
1. `recall` 撤回消息的延时，单位秒，默认0秒
1. `plain` 是否检查文本，默认 true
1. `image` 是否检查图片，默认 false
1. `audio` 是否检查语音，默认 false

### 避开审查

拥有权限 `io.github.gnuf0rce.content-censor:no-censor` 的用户将不会被检测

### 指令

1. `/censor` 测试是否有有违规  
    示例: `/censor cnm`
2. `/censor-record from [sender] [date]` 查看 sender (消息的发送者) 相关的违规记录
   示例: `/censor from 123456 2022-07-24`
3. `/censor-record target [subject] [date]` 查看 subject (消息的接收者) 相关的违规记录
   示例: `/censor target 789456 2022-07-24`

## 安装

### MCL 指令安装

**请确认 mcl.jar 的版本是 2.0.0+**  
`./mcl --update-package io.github.gnuf0rce:mirai-content-censor --channel stable --type plugin`

### 手动安装

1. 运行 [Mirai Console](https://github.com/mamoe/mirai-console) 生成`plugins`文件夹
1. 从 [Releases](https://github.com/cssxsh/mirai-content-censor/releases) 下载`jar`并将其放入`plugins`文件夹中

## TODO

- [x] 配置禁言时间
- [ ] QPS延迟
- [ ] 视频内容审核
- [x] 转发消息内容审核
