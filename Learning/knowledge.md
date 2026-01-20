2025年12月16日
 - `Javalin`的路由注册必须在`Javalin.create(...).start(...)`之后, 必须先启动服务器才可以
 - 如果想关闭`Javalin`使用的`slf4j`日志器的那一大串输出, 你必须在创建服务器实例之前调用它
 - 在`jbang`中, 如果想要多文件编程, 并且希望能够正确识别到(那肯定要正确检测到啊), 需要在文件头部使用`//SOURCES {filepath}`
 - `jbang`的文件顶部配置项必须与双正斜杠紧贴, 其间不允许出现其他空白字符

2025年12月17日
 - 你不需要使用 Java 的`resources`目录, 你也没那么多必须打包进`jar`包的资源, 最好的做法是基于工作目录的相对路径来获取文件内容, 至少你还可以在脚本运行时修改前端文件

2026年1月7日
 - 对于`Javalin`, 它内置了一个`Jackson`的接口来实现`Json`解析, 但当我们需要使用其它`Json`实现, 比如`Gson`时, 就需要继承`io.javalin.json.JsonMapper`来自己实现一个`Mapper`, 并且在`Javalin`服务器启动时替换`config`中的`Mapper`

2026年1月18日
 - `List<Map<String, Object>>` 会直接返回一个空的 `[]`, 所以 `.get(Object)` 会直接抛出`java.util.NoSuchElementException`, 导致 `.isEmpty` 返回 `true` ... 纯有病。

2026年1月20日
 - `gradlew run` 运行程序时, Java 根本无法获取到实际的命令运行方式, 出来的不可能是 `/path/to/gradlew run`, 获取到的只有`{域名反写}.{主类} {整个参数列表}`
   <p>目前不确定直接运行jar包会发生什么
 - `java -jar [path/to/jar]` 得到的是 `{path/to/jar} {arg list}`, 这里的路径是你输入的路径
 - 01点02分 买个小甜水去
 - 02点17分 这样吧, 先不纠结隐藏什么的了, 先直接展示算了

2026年1月21日
 - 别设完`ctx.status(code)`就万事大吉了, 还得`return`...今天因为这玩意儿干了两次复查了, 可长点心吧...