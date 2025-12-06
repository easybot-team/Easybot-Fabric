# EasyBot for Fabric

![Minecraft](https://img.shields.io/badge/Minecraft-1.19.x%20|%201.20.x-green.svg) ![Fabric API](https://img.shields.io/badge/Fabric%20API-Required-blue.svg) ![License](https://img.shields.io/badge/License-MIT-yellow.svg)

**EasyBot for Fabric** 是强大地跨平台机器人框架 [EasyBot](https://github.com/easybot-team) 的官方 Fabric 服务端插件。它作为一个桥梁，将您的 Fabric 服务器无缝连接到 EasyBot 主程序，从而实现跨服务器、跨平台（如 QQ 群）的消息同步、数据互通和统一管理。详见[文档](https://docs.inectar.cn/)

## ✨ 功能特性

-   **无缝消息同步**: 实时同步游戏内聊天到其他平台，并将其他平台的消息以丰富的格式（支持 `@玩家`、可点击的图片链接等）展示在游戏中。
-   **事件监听与上报**: 实时监听并上报玩家的加入、退出、死亡等关键事件，为主程序提供数据支持。
-   **远程命令执行**: 允许管理员通过主程序远程在 Fabric 服务器上执行指令，并获取返回结果。
-   **服务器状态监控**: 主程序可以随时获取服务器的在线玩家列表、版本等详细信息。
-   **内置管理命令**: 提供 `/easybot` 命令，方便在游戏内直接管理插件。
-   **详见[文档](https://docs.inectar.cn/)**

## 🚀 安装与配置

### 1. 前置要求
- 您的服务器已安装 Fabric 核心。
- 您已在某处（可以是同一台机器或云服务器）部署并运行了 EasyBot 主程序。
- 您已阅读 easybot [文档](https://docs.inectar.cn/)

### 2. 安装插件
1.  前往 Releases 页面，下载与您服务器 Minecraft 版本对应的 `.jar` 文件。
2.  将下载的 `.jar` 文件放入您服务器的 `mods` 文件夹中。
3.  启动一次服务器，插件会自动在 `config` 目录下生成 `easybot-fabric.json` 配置文件。

### 3. 配置插件
关闭服务器，编辑 `config/easybot-fabric.json` 文件，然后重新启动。

- **`serverUrl`**: EasyBot 主程序 WebSocket 服务的地址。
- **`authToken`**: 用于和主程序认证的令牌，必须与主程序设置的一致。
- **`debug`**: 是否开启调试模式。开启后，控制台会输出详细的通信日志，便于排查问题。

## 🤝 贡献

如果您想为项目做出贡献，请阅读我们的 [贡献者指南](CONTRIBUTING.md)。


