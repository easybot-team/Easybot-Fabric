# EasyBot-Fabric

EasyBot-Fabric是一个Minecraft Fabric模组，用于通过WebSocket将Minecraft服务器与聊天平台（如QQ、Discord等）连接起来。

## 功能特点

- 通过WebSocket与聊天平台桥接服务连接
- 在Minecraft服务器和聊天平台之间同步消息
- 支持从聊天平台执行Minecraft服务器命令
- 可配置的消息格式和功能选项

## 安装要求

- Minecraft 1.20.1
- Fabric Loader 0.14.21+
- Fabric API
- Java 17+

## 安装方法

1. 安装[Fabric Loader](https://fabricmc.net/use/)和[Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
2. 下载EasyBot-Fabric的最新版本并放入服务器的`mods`文件夹
3. 启动服务器，模组将自动生成配置文件
4. 根据需要编辑配置文件

## 配置

首次运行后，配置文件将在`config/easybotfabric.json`生成。你可以编辑以下选项：

```json
{
  "websocket": {
    "enabled": true,
    "uri": "ws://localhost:8080/minecraft",
    "reconnectDelay": 5000
  },
  "messages": {
    "playerJoin": true,
    "playerLeave": true,
    "playerChat": true,
    "playerDeath": true,
    "serverStart": true,
    "serverStop": true
  }
}
```

## 命令

- `/easybot reload` - 重新加载配置文件
- `/easybot connect` - 连接到WebSocket服务器
- `/easybot disconnect` - 断开与WebSocket服务器的连接
- `/easybot status` - 显示连接状态

## 开发

### 构建项目

```bash
./gradlew build
```

构建完成后，JAR文件将位于`build/libs/`目录中。

### 设置开发环境

```bash
./gradlew genSources
./gradlew idea  # 对于IntelliJ IDEA
./gradlew eclipse  # 对于Eclipse
```

## 许可证

本项目采用MIT许可证 - 详见[LICENSE](LICENSE)文件。

## 贡献

欢迎提交问题报告和拉取请求#