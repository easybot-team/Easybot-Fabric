# 贡献者指南

欢迎您考虑为 EasyBot Fabric 项目做出贡献！本指南将帮助您了解项目结构、开发流程和贡献规范。

## 项目介绍

EasyBot Fabric 是强大的跨平台机器人框架 EasyBot 的官方 Fabric 服务端插件。它作为桥梁，将 Fabric 服务器无缝连接到 EasyBot 主程序，实现跨服务器、跨平台（如 QQ 群）的消息同步、数据互通和统一管理。

## 开发环境搭建

### 1. 前置要求

- Java 17 或更高版本
- Git
- IDE（推荐 IntelliJ IDEA）

### 2. 克隆仓库

```bash
git clone https://github.com/Easybot-team/EasyBot-Fabric.git
cd EasyBot-Fabric
```

### 3. 导入项目

- 使用 IntelliJ IDEA 打开项目根目录
- 等待 Gradle 自动下载依赖并配置项目

### 4. 构建项目

```bash
# 构建所有版本的模组
./gradlew build

# 构建特定版本（例如 1.21.1）
./gradlew :1.21.1:build
```

## 项目结构

### 主项目结构

```
├── src/main/java/org/easybot/easybotfabric/
│   ├── EasyBotFabric.java          # 模组主类
│   ├── MessageHandler.java         # 消息处理器
│   ├── bridge/                     # 桥接客户端相关类
│   ├── command/                    # 命令处理器
│   ├── config/                     # 配置相关类
│   ├── duck/                       # 扩展接口
│   ├── listener/                   # 事件监听器
│   ├── mixin/                      # Mixin 类
│   └── util/                       # 工具类
├── versions/                       # 不同 Minecraft 版本的实现
│   ├── 1.20.4/
│   ├── 1.21.1/
│   └── 1.21.8/
├── build.gradle                    # 主构建配置
├── gradle.properties               # Gradle 属性配置
└── settings.gradle                 # Gradle 项目设置
```

### 版本特定结构

每个版本目录（如 `versions/1.21.1/`）包含：

```
├── src/main/java/org/easybot/easybotfabric/bridge/
│   └── FabricBridgeBehavior.java   # 版本特定的桥接行为实现
└── src/main/resources/
    ├── fabric.mod.json             # Fabric 模组元数据
    └── mixins.easybotfabric.json   # Mixin 配置
```

## 开发流程

### 1. 创建分支

```bash
# 从 main 分支创建新特性分支
git checkout -b feature/your-feature-name

# 或从 main 分支创建修复分支
git checkout -b fix/your-fix-name
```

### 2. 编写代码

- 遵循项目的代码规范（见下文）
- 确保代码在所有支持的 Minecraft 版本上兼容
- 为新功能添加适当的注释

### 3. 测试代码

```bash
# 运行特定版本的客户端
./gradlew :1.21.1:runClient

# 运行特定版本的服务器
./gradlew :1.21.1:runServer
```

### 4. 提交代码

```bash
# 添加更改
git add .

# 提交更改（遵循提交规范）
git commit -m "feat: 添加新功能"
```

### 5. 推送分支

```bash
git push origin feature/your-feature-name
```

### 6. 创建 Pull Request

1. 访问 GitHub 仓库页面
2. 点击 "Pull requests" 标签
3. 点击 "New pull request"
4. 选择您的分支和目标分支（通常是 main）
5. 填写 PR 描述，包括更改内容和动机
6. 提交 PR

## 代码规范

### Java 代码规范

- 使用 4 个空格进行缩进
- 行长度不超过 120 个字符
- 使用 CamelCase 命名类、方法和变量
- 类名首字母大写，方法和变量名首字母小写
- 使用 Javadoc 注释公共类和方法

### 示例

```java
/**
 * 这是一个示例类的 Javadoc 注释
 */
public class ExampleClass {
    private final Logger logger = LoggerFactory.getLogger(ExampleClass.class);
    
    /**
     * 这是一个示例方法的 Javadoc 注释
     * @param param1 参数1的描述
     * @return 返回值的描述
     */
    public String exampleMethod(String param1) {
        // 代码实现
        return "result";
    }
}
```

### 版本兼容性

- 核心功能应在主项目中实现
- 版本特定的功能应在对应版本目录下实现
- 避免在版本特定代码中重复实现核心功能

## 提交规范

请遵循以下提交消息格式：

```
<类型>: <描述>

[可选的详细描述]
```

### 类型

- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `style`: 代码风格调整（不影响功能）
- `refactor`: 代码重构（不添加新功能或修复 bug）
- `test`: 添加或修改测试
- `chore`: 构建过程或辅助工具的变动

### 示例

```
feat: 添加玩家死亡事件监听

- 实现玩家死亡事件的捕获
- 将死亡事件发送到 EasyBot 主程序
- 添加配置选项控制是否启用该功能
```

## CI/CD

项目使用 GitHub Actions 进行持续集成和发布：

- `gradle.yml`: 自动构建和测试所有版本
- `gradle-publish.yml`: 自动发布新版本到 GitHub Releases

## 问题报告

如果您发现 bug 或有功能建议，请通过以下方式报告：

1. 在 GitHub Issues 中创建新问题
2. 提供详细的描述，包括：
   - Minecraft 版本
   - Fabric API 版本
   - EasyBot Fabric 版本
   - 问题重现步骤
   - 相关日志信息

## 社区规范

- 尊重其他贡献者
- 保持讨论积极和建设性
- 遵循项目的代码和文档规范
- 帮助新贡献者融入社区

## 联系方式

- GitHub: [Easybot-team/EasyBot-Fabric](https://github.com/Easybot-team/EasyBot-Fabric)
- 文档: [https://docs.inectar.cn/](https://docs.inectar.cn/)

感谢您的贡献！