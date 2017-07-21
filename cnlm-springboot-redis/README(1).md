#Springboot Redis分布式集群（1）- Linux下redis安装与使用

@(Markdown博客)

- redis官网地址：[http://www.redis.io/](http://www.redis.io/)
- 最新版本：4.0.0

##在Linux下安装Redis非常简单，具体步骤如下（官网有说明）：

1. 下载源码，解压缩后编译源码。

```bash
$ wget http://download.redis.io/releases/redis-4.0.0.tar.gz

$ tar xzf redis-4.0.0.tar.gz

$ cd redis-4.0.0

$ make & make install
```
2. 编译完成后，在src目录下，有3个可执行文件redis-server、redis-benchmark、redis-cli，redis-4.0.0目录有redis.conf，然后拷贝到一个目录下。
```bash
mkdir -p /usr/local/redis

cp redis-server /usr/local/redis

cp redis-benchmark /usr/local/redis

cp redis-cli /usr/local/redis

cp redis.conf /usr/local/redis

cd /usr/local/redis
```

3. 启动Redis服务
```bash
$ redis-server redis.conf
```
4. 然后重新开一个xshell窗口使用客户端测试一下是否启动成功。

```bash
[root@cnlm redis]# redis-cli 
127.0.0.1:6379> get key
(nil)
127.0.0.1:6379> set key cnlm.me
OK
127.0.0.1:6379> get key
"cnlm.me"
127.0.0.1:6379> 
```
5. 设置redis以守护进程方式在后台运行
- 先将redis.conf拷贝到/etc/redis.conf
```bash
cp redis.conf /etc/redis.conf
```
```bash
vi /etc/redis.conf  
#查找daemonize no改为yes  
#以守护进程方式运行  
daemonize yes  
#修改dir ./为/usr/local/redis,  
#默认的话redis-server启动时会在当前目录生成或读取dump.rdb  
#所以如果在根目录下执行redis-server /etc/redis.conf的话,  
#读取的是根目录下的dump.rdb,为了使redis-server可在任意目录下执行  
#所以此处将dir改为绝对路径  
dir /usr/local/redis  
#修改appendonly为yes  
#指定是否在每次更新操作后进行日志记录，  
#Redis在默认情况下是异步的把数据写入磁盘，  
#如果不开启，可能会在断电时导致一段时间内的数据丢失。  
#因为 redis本身同步数据文件是按上面save条件来同步的，  
#所以有的数据会在一段时间内只存在于内存中。默认为no  
appendonly yes  
#将redis添加到自启动中  
echo "/usr/local/redis-server /etc/redis.conf" >> /etc/rc.d/rc.local  
#启动redis  
redis-server /etc/redis.conf  
#查看redis是否己启动  

[root@cnlm ~]# ps -ef|grep redis
root      2738     1  0 11:15 ?        00:00:00 redis-server *:6379
root      2983  2893  0 11:52 pts/0    00:00:00 grep --color=auto redis
[root@cnlm ~]# 
  
```

6. 开放redis端口

```bash 
#关闭防火墙  
service iptables stop  
vi /etc/sysconfig/iptables  
#添加  
-A INPUT -m state --state NEW -m tcp -p tcp --dport 6379 -j ACCEPT  
#重启防火墙  
service iptables restart  
```