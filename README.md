# Springboot集成quartz之让项目飞起来（第一期）
- 开发工具：
1. IDEA 2017.1.5 
2. Maven项目管理


##1. 新建springboot模块
   - 模块名称：`cnlm-springboot-quartz`
![项目结构](./1.png)

##2. 重命名springboot启动类为Application
```
@(Markdown博客)SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```
##3. 运行：果然预料之中，运行不起来，报错，异常信息：
```text
2017-07-15 21:31:38.178  WARN 5320 --- [           main] s.c.a.AnnotationConfigApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'dataSource' defined in class path resource [org/springframework/boot/autoconfigure/jdbc/DataSourceConfiguration$Tomcat.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.apache.tomcat.jdbc.pool.DataSource]: Factory method 'dataSource' threw exception; nested exception is org.springframework.boot.autoconfigure.jdbc.DataSourceProperties$DataSourceBeanCreationException: Cannot determine embedded database driver class for database type NONE. If you want an embedded database please put a supported one on the classpath. If you have database settings to be loaded from a particular profile you may need to active it (no profiles are currently active).
2017-07-15 21:31:38.182  INFO 5320 --- [           main] utoConfigurationReportLoggingInitializer : 

Error starting ApplicationContext. To display the auto-configuration report re-run your application with 'debug' enabled.
Disconnected from the target VM, address: '127.0.0.1:43215', transport: 'socket'
2017-07-15 21:31:38.187 ERROR 5320 --- [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 

***************************
APPLICATION FAILED TO START
***************************

Description:

Cannot determine embedded database driver class for database type NONE

Action:

If you want an embedded database please put a supported one on the classpath. If you have database settings to be loaded from a particular profile you may need to active it (no profiles are currently active).


Process finished with exit code 1
```

初步判断是由于没有设置数据源导致，
resources/application.properties文件加入数据库连接配置：
```text
## master 数据源配置
master.datasource.url=jdbc:mysql://localhost:3306/cnlm-blog?useUnicode=true&characterEncoding=utf8
master.datasource.username=root
master.datasource.password=123456
master.datasource.driverClassName=com.mysql.cj.jdbc.Driver
server.port=22222
# 配置mapper的扫描，找到所有的mapper.xml映射文件
mybatis.mapperLocations=classpath:mapper/**/*.xml
# 加载全局的配置文件
mybatis.configLocation=classpath:mybatis-config.xml
```

pom.xml文件中加入mysql数据库java版驱动依赖
```xml
<!-- MySql数据库驱动 版本号6.0.6 -->
<!-- https://mvnrepository.com/artifact/mysql/mysql-connector-java -->
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<version>${mysql-connector-java.version}</version>
</dependency>
```
pom.xml文件中加入阿里巴巴开源数据库框架druid的依赖
```xml
<!-- druid阿里巴巴数据库连接池 -->
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>druid</artifactId>
	<version>${druid.version}</version>
</dependency>
```
至此，创建一个类用于初始化数据库连接
```java
package me.cnlm.springboot.quartz.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * @author zhuxl@paxsz.com
 * @time 2017/5/4
 */
@Configuration
// 扫描 Mapper 接口并容器管理
@MapperScan(basePackages = {MasterDataSourceConfig.PACKAGE_DAO}, sqlSessionFactoryRef = "masterSqlSessionFactory")

public class MasterDataSourceConfig {

    static final String PACKAGE_DAO = "me.cnlm.springboot.quartz.dao";

    @Value("${mybatis.mapperLocations}")
    private String mapperLocation;

    @Value("${mybatis.configLocation}")
    private String configLocation;

    @Value("${master.datasource.url}")
    private String url;

    @Value("${master.datasource.username}")
    private String user;

    @Value("${master.datasource.password}")
    private String password;

    @Value("${master.datasource.driverClassName}")
    private String driverClass;

    @Bean(name = "masterDataSource")
    @Primary
    public DataSource masterDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean(name = "masterTransactionManager")
    @Primary
    public DataSourceTransactionManager masterTransactionManager() {
        return new DataSourceTransactionManager(masterDataSource());
    }

    @Bean(name = "masterSqlSessionFactory")
    @Primary
    public SqlSessionFactory masterSqlSessionFactory(@Qualifier("masterDataSource") DataSource masterDataSource)
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(masterDataSource);
        sessionFactory.setConfigLocation(new DefaultResourceLoader().getResource(configLocation));
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(mapperLocation));

        return sessionFactory.getObject();
    }
}

```

##4. 再次运行，还是惊吓！！！
这次异常：
```text
Caused by: org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.apache.ibatis.session.SqlSessionFactory]: Factory method 'masterSqlSessionFactory' threw exception; nested exception is java.io.FileNotFoundException: class path resource [mapper/] cannot be resolved to URL because it does not exist
	at org.springframework.beans.factory.support.SimpleInstantiationStrategy.instantiate(SimpleInstantiationStrategy.java:189) ~[spring-beans-4.3.9.RELEASE.jar:4.3.9.RELEASE]
	at org.springframework.beans.factory.support.ConstructorResolver.instantiateUsingFactoryMethod(ConstructorResolver.java:588) ~[spring-beans-4.3.9.RELEASE.jar:4.3.9.RELEASE]
	... 17 common frames omitted
Caused by: java.io.FileNotFoundException: class path resource [mapper/] cannot be resolved to URL because it does not exist
```
....
....
....
期间还需要添加相关依赖，最后在运行的时候已经习惯了，还是异常：
```
Unregistering JMX-exposed beans on shutdown
```
网上一搜，答案一大堆，原来是没有引入嵌入式tomcat，springboot默认的嵌入容器是tomcat，也可以自定义使用jetty，网上很多答案是加的：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
</dependency>

```
但我加的是下面这个，它包括了上面的tomcat依赖
```xml
<!-- Spring Boot Web 依赖 -->
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
##5. 再次启动，这次终于成功运行起来了。另外需要说明的是，上述我引入了mybatis的额外配置文件以及mybatis写sql的xml扫描路径，所以需要添加
- resources/mybatis 
- resources/mybatis-config.xml


本期已结束，至此项目已经可以飞起来了，下期：
- **Springboot集成quartz之定时打印Hello World（第二期）**

##**欢迎加入技术交流QQ群566654343**`（菜鸟联盟 ）`