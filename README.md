## 多线程爬虫与ES数据分析
******
### 自动化代码检查
* 使用`maven-checkstyle-plugin`与`spotbugs`插件对代码进行自动化格式检查，使用`circleCI`自动化检查pr的代码
### 爬虫与多线程
* 使用`org.apache.httpcomponents`进行http请求，并使用`org.jsoup`解析请求到的`html`页面
### 数据库持久化存储
* 使用`H2`数据库并使用传统`JDBC`方式进行数据库的更新与访问
* 使用数据库自动化迁移插件`flyway`
* 使用`mybatis`简化数据库操作
* 将数据库模块替换成`MySQL`
### 数据库的查询与`Elasticsearch`搜索引擎
* 数据库的索引，以及传统关系型数据库的基本结构，存储数据的数据结构（b书与b+树）基于联合索引优化`SQL`语句
* `Elasticsearch`的简单使用，与传统数据库的区别，基于倒排索引更快的查询速度
