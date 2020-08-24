
<a href="https://github.com/ymm-tech/easy-byte-coder/blob/master/README.md" title="English_description">English description</a>

<a href="https://github.com/ymm-tech/easy-byte-coder/wiki">WIKI使用手册</a>

# 简介

easy-byte-coder 一款字节码注入框架，在 JVM 平台非侵入式字节码注入的解决方案，帮助java应用开发者，快速开发字节码注入程序,无需关心底层instrument原理以及实现细节，用户使用plugin的形式快速提供aop能力，并且可以选择使用静态编织(agent)与动态编织(attach)两种方案，plugin编写起来非常简单。



## 启动方法

Attach(动态注入)模式时,此工具依赖被注入应用的jvm运行时的环境变量，请确认JAVA_HOME是否存在，Agent(静态注入)模式无需此配置

attach模式启动:

```
java -Xbootclasspath/a:$JAVA_HOME/lib/tools.jar -jar bootstrap.jar
```

选择注入进程：

![image-20200818201959949](./img/image-20200818201959949.png)

如果输出：

```
Attaching to target JVM with PID: 91420
Attached to target JVM and loaded Java agent successfully
```

则表示注入成功

在一些情况下，如修改jdk源码，对jdk的方法进行注入，或是启动时就需要进行大量的修改，此时可以使用agent模式

agent通过增加jvm参数启动plugins=后的jar包名是 easy-byte-coder编译后生成的jar包

agent模式启动:

```
-javaagent:/Users/xujie/work/ymm/jar/agent.jar=plugins=systemtime.jar
```



## 工作原理：

### 切点:

用户根据框架要求，继承接口，指定需要修改字节码的class中的method的切点进行注入，目前允许用户使用的切点范围：

- LocalVariables 在方法中加入局部变量

- Before 在方法运行之前修改
- After 在方法返回之前修改
- Catch 自定义捕获方法异常
- Body 替换整个方法体

### 类隔离

通过不同的classLoade实现类隔离，使应用类与zodiac类不会相互干扰，并且实现了资源管理器ZodiacResourceObjectPool，使得用户可以方便的切换获取应用空间运行时类加载器与zodiac类加载器，方便用户调用应用空间的类对象，复用原有应用提供的一些基础功能，无需重新在zodiac中编写，加速开发效率。

### 资源管理器

类资源的统一管理工具，使用户方便的获取“应用空间”运行时加载的类与获取Zodiac空间加载的类

### spi-plugin

采用java spi机制加载plugin，方便用户编写plugin，只需继承接口，编写接口中需要实现的方法，即可完成对应用的字节码注入，无需关心底层原理

# 使用场景

需要快速开发字节码注入应用的场景如：

- 无侵入的打点上报
- 无侵入的线上故障模拟
- 无侵入的线上错误代码的临时修复
- 本地调试代码时无感知的mock数据返回
- 线上流量录制回放结果对比

# 交流渠道

# 贡献者名单（排名不分前后）
- [yang.gao](https://github.com/gaozhaoyanghe)
- [jie.xu](https://github.com/njxjxj)
- [xinhua.yang](https://github.com/yangxinghua0716)

# 敬请期待（衍生工具开源计划）

1. Virgo 微服务本地mock调试工具
2. Gemini 微服务流量录制回放工具 
