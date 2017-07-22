#Springboot Redis分布式集群（3）- redis集群安装与配置

@(Markdown博客)

- Redis分布式缓存的实现方式
Redis从Redis3.0后开始支持Redis分布式缓存，可以从三种不同的方式来搭建Redis分布式缓存实现。 
     1. 客户端分片 
     这种方案将分片工作放在业务程序端，程序代码根据预先设置的路由规则，直接对多个Redis实例进行分布式访问。它的好处是实现方法和代码都自己可控，可随时调整，不足之处是这实际上是一种静态分片技术。Redis实例的增减，都得手工调整分片程序。 
     2. 代理分片 
     这种方案，将分片工作交给专门的代理程序来做。代理程序接收到来自业务程序的数据请求，根据路由规则，将这些请求分发给正确的Redis实例并返回给   业务程序。这种机制下，一般会选用第三方代理程序（而不是自己研发），因为后端有多个Redis实例，所以这类程序又称为分布式中间件。目前有一些框架专门来做代理分片的，如国内的Codis。 
     3. Redis Cluster
     在这种机制下，没有中心节点（和代理模式的重要不同之处），Redis Cluster将所有Key映射到16384个Slot中，集群中每个Redis实例负责一部分，业务程序通过集成的Redis Cluster客户端进行操作。客户端可以向任一实例发出请求，如果所需数据不在该实例中，则该实例引导客户端自动去对应实例读写数据。 
     
- Redis分布式缓存基础 
     1. redis监听端口 
     每个Redis节点使用两个端口，一个提供对外服务端口，默认6379，另一个用于集群间同步数据通知，端口号是在对外服务端口上加10000，即默认为16379. 每一个节点都需要知道其他节点的情况，这里就包括其他节点负责处理哪些键值对。这也就是客户端向任一实例发出请求，如果所需数据不在该实例中，则该实例引导客户端自动去对应实例读写数据。 
     2. 数据分片（Sharding） 
     所有key 分布在16384个hash slot上，数据分组及迁移都是以hash slot为单位。使用CRC16算法计算一个key应该落在哪个hash slot上。 
     Redis集群采用的是hash slot分片来完成的。例如： 
            Node A contains hash slots from 0 to 5500. 
            Node B contains hash slots from 5501 to 11000. 
            Node C contains hash slots from 11001 to 16384. 
     3. 主从模式（Master-Salve） 
     Redis cluster的拓扑结构，由3个以上Master节点形成数据分片集群，覆盖所有16384个hash slots, 数据只能在Master节点间以slot为单位迁移。 
     每个Master可以有多个replica节点（即slave）, 以防灾备，当master宕机时，集群会通过选举晋升这个master的一个slave节点变为master, 继续提供服务。master宕机可能会丢失写的数据。因为master在接收请求处理完后会立即返回客户端，master如果在同步到slave之前就down了，就会lose write.（机制决定，不可避免）。 
     4. Redis Cluster失效 
     在下面的两种情况下，Redis Cluster就失效，即不对外提供服务了。 
     1）如果集群任意master挂掉,且当前master没有slave.集群进入fail状态,也可以理解成集群的slot映射[0-16383]不完成时进入fail状态。 
     2）如果集群超过半数以上master挂掉，无论是否有slave集群进入fail状态。 
     5.Redis Cluster 数据迁移 
        Redis支持在线进行数据迁移。Redis默认的slave节点是不支持写操作的，我们需要修改它的配置 
        - `#slave-read-only yes` 
        - `slave-read-only no` 
     6. 客户端框架 
     从官方网站上看，Redis目前有很多客户端框架，在Java中，目前用得比较多的是Spring Data Redis和Jedis。
##1. redis集群环境的安装
  - 安装前置，需要安装ruby和rubygems如下依赖包，若已安装，系统会自动忽略。
  ```bash
  yum -y install gcc openssl-devel libyaml-devel libffi-devel readline-devel zlib-devel gdbm-devel ncurses-devel gcc-c++ automake autoconf
  ```
  - 下载ruby-2.2.1.tar.gz
  ```bash
  wget https://cache.ruby-lang.org/pub/ruby/2.2/ruby-2.2.1.tar.gz
  ```
  - 解压安装ruby
  ```bash
  tar -zxvf ruby-2.2.1.tar.gz
  cd ruby-2.2.1  
  ./configure -prefix=/usr/local/ruby    
  make  
  make install    
  cp ruby /usr/local/bin
  ```
  - 下载rubygems-2.6.12.tgz
  ```bash
  wget https://rubygems.org/rubygems/rubygems-2.6.12.tgz
  ```
  - 解压并安装rubygems
  ```bash
  tar -xvzf rubygems-2.6.12.tgz  
  cd rubygems-2.6.12  
  ruby setup.rb  
  cp bin/gem /usr/local/bin
  ```
  - 下载redis-3.3.3.gem（这个是用于创建redis集群时用到）
  ```bash
  wget https://rubygems.org/downloads/redis-3.3.3.gem
  ```
  - 在redis-3.3.3.gem当前目录执行如下命令：
  ```bash
  gem install -l ./redis-3.3.3.gem
  ```
  到此，redis集群环境已经搭建好了，接下来需要配置redis集群
  
##2. redis集群的配置
  - 进入/usr/local/redis目录并创建6个集群节点目录
  ```bash
  -rw-r--r-- 1 root root     277 Jul 21 23:28 appendonly.aof
  -rw-r--r-- 1 root root     204 Jul 22 01:20 dump.rdb
  -rwxr-xr-x 1 root root 2421328 Jul 21 12:27 redis-benchmark
  -rwxr-xr-x 1 root root 2574930 Jul 21 12:27 redis-cli
  -rwxr-xr-x 1 root root 5687334 Jul 21 12:27 redis-server
  -rw-r--r-- 1 root root   57772 Jul 22 08:53 redis.conf
  [root@cnlm redis]# mkdir -p redis-cluster/7000
  [root@cnlm redis]# mkdir -p redis-cluster/7001
  [root@cnlm redis]# mkdir -p redis-cluster/7002
  [root@cnlm redis]# mkdir -p redis-cluster/7003
  [root@cnlm redis]# mkdir -p redis-cluster/7004
  [root@cnlm redis]# mkdir -p redis-cluster/7005
  [root@cnlm redis]# ll
  total 10512
  -rw-r--r-- 1 root root     277 Jul 21 23:28 appendonly.aof
  -rw-r--r-- 1 root root     204 Jul 22 01:20 dump.rdb
  -rwxr-xr-x 1 root root 2421328 Jul 21 12:27 redis-benchmark
  -rwxr-xr-x 1 root root 2574930 Jul 21 12:27 redis-cli
  drwxr-xr-x 8 root root    4096 Jul 22 08:53 redis-cluster
  -rwxr-xr-x 1 root root 5687334 Jul 21 12:27 redis-server
  -rw-r--r-- 1 root root   57772 Jul 22 08:53 redis.conf
  [root@cnlm redis]# 
  ```
  - 首先修改redis.conf参数：
  ```bash
  bind 0.0.0.0  //意味着允许所有主机连接
  port 7000  //每个Redis实例的端口必须是唯一的 
  cluster-enabled yes //支持集群 
  cluster-config-file nodes-7000.conf //nodes-7000.conf这个文件不用我们去编辑 
  pidfile /var/run/redis_7000.pid   //这个文件也不需要编辑
  cluster-node-timeout 5000 
  appendonly yes
  ```
  - 其次将redis.conf、redis-server、redis-cli、redis-benchmark拷贝到6个节点的目录里
  ```bash
  [root@cnlm redis]# cp redis.conf redis-server redis-cli redis-benchmark redis-cluster/7000/
  [root@cnlm redis]# cp redis.conf redis-server redis-cli redis-benchmark redis-cluster/7001/
  [root@cnlm redis]# cp redis.conf redis-server redis-cli redis-benchmark redis-cluster/7002/
  [root@cnlm redis]# cp redis.conf redis-server redis-cli redis-benchmark redis-cluster/7003/
  [root@cnlm redis]# cp redis.conf redis-server redis-cli redis-benchmark redis-cluster/7004/
  [root@cnlm redis]# cp redis.conf redis-server redis-cli redis-benchmark redis-cluster/7005/
  [root@cnlm redis]# cd redis-cluster/7000/
  [root@cnlm 7000]# ll
  total 10500
  -rwxr-xr-x 1 root root 2421328 Jul 22 08:58 redis-benchmark
  -rwxr-xr-x 1 root root 2574930 Jul 22 08:58 redis-cli
  -rwxr-xr-x 1 root root 5687334 Jul 22 08:58 redis-server
  -rw-r--r-- 1 root root   57772 Jul 22 08:58 redis.conf
  ```
  - 每个节点下必须修改的地方：redis.conf中
  ```bash
  port 7000    //集群节点端口号
  pidfile /var/run/redis_7000.pid   //进程pid存放文件
  cluster-config-file nodes-7000.conf  //每个节点有个这样的配置文件，不需要我们编辑，它是redis节点自动创建和更新，每个集群节点的配置必须不能一样
  ```
  - 修改完6个节点后将6个节点分别运行起来
  ```bash
  [root@cnlm redis-cluster]# ./7000/redis-server 7000/redis
  redis-benchmark  redis-cli        redis-server     redis.conf       
  [root@cnlm redis-cluster]# ./7000/redis-server 7000/redis.conf 
  24384:C 22 Jul 09:33:17.079 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
  24384:C 22 Jul 09:33:17.079 # Redis version=4.0.0, bits=64, commit=00000000, modified=0, pid=24384, just started
  24384:C 22 Jul 09:33:17.079 # Configuration loaded
  [root@cnlm redis-cluster]# ./7001/redis-server 7001/redis.conf 
  24389:C 22 Jul 09:33:25.685 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
  24389:C 22 Jul 09:33:25.686 # Redis version=4.0.0, bits=64, commit=00000000, modified=0, pid=24389, just started
  24389:C 22 Jul 09:33:25.686 # Configuration loaded
  [root@cnlm redis-cluster]# ./7002/redis-server 7002/redis.conf 
  24394:C 22 Jul 09:33:32.133 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
  24394:C 22 Jul 09:33:32.133 # Redis version=4.0.0, bits=64, commit=00000000, modified=0, pid=24394, just started
  24394:C 22 Jul 09:33:32.133 # Configuration loaded
  [root@cnlm redis-cluster]# ./7003/redis-server 7003/redis.conf 
  24399:C 22 Jul 09:33:38.816 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
  24399:C 22 Jul 09:33:38.816 # Redis version=4.0.0, bits=64, commit=00000000, modified=0, pid=24399, just started
  24399:C 22 Jul 09:33:38.816 # Configuration loaded
  [root@cnlm redis-cluster]# ./7004/redis-server 7004/redis.conf 
  24404:C 22 Jul 09:33:45.155 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
  24404:C 22 Jul 09:33:45.155 # Redis version=4.0.0, bits=64, commit=00000000, modified=0, pid=24404, just started
  24404:C 22 Jul 09:33:45.155 # Configuration loaded
  [root@cnlm redis-cluster]# ./7005/redis-server 7005/redis.conf 
  24409:C 22 Jul 09:33:51.709 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
  24409:C 22 Jul 09:33:51.709 # Redis version=4.0.0, bits=64, commit=00000000, modified=0, pid=24409, just started
  24409:C 22 Jul 09:33:51.709 # Configuration loaded
  [root@cnlm redis-cluster]# ps -ef|grep redis
  root     24385     1  0 09:33 ?        00:00:00 ./7000/redis-server 0.0.0.0:7000 [cluster]
  root     24390     1  0 09:33 ?        00:00:00 ./7001/redis-server 0.0.0.0:7001 [cluster]
  root     24395     1  0 09:33 ?        00:00:00 ./7002/redis-server 0.0.0.0:7002 [cluster]
  root     24400     1  0 09:33 ?        00:00:00 ./7003/redis-server 0.0.0.0:7003 [cluster]
  root     24405     1  0 09:33 ?        00:00:00 ./7004/redis-server 0.0.0.0:7004 [cluster]
  root     24410     1  0 09:33 ?        00:00:00 ./7005/redis-server 0.0.0.0:7005 [cluster]
  root     24415  9686  0 09:33 pts/0    00:00:00 grep --color=auto redis
  ```
  - 到此，6个节点分别都已经运行起来，但还不是集群，现创建集群，不过在创建集群的时候遇到了如下错误:
  ```bash
  [root@cnlm redis-cluster]# cd /env/redis-4.0.0/src/
  [root@cnlm src]# ./redis-trib.rb  create --replicas 1 127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005
  >>> Creating cluster
  [ERR] Node 127.0.0.1:7000 is not empty. Either the node already knows other nodes (check with CLUSTER NODES) or contains some key in database 0.
  [root@cnlm src]#
  ```
  - 网上搜索一下，原因为原来使用redis非集群的时候，产生了配置或存储文件appendonly.aof、dump.rdb，将这两个文件删除后重启redis各节点
  ```bash
  [root@cnlm src]# ./redis-trib.rb  create --replicas 1 127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005
  >>> Creating cluster
  >>> Performing hash slots allocation on 6 nodes...
  Using 3 masters:
  127.0.0.1:7000
  127.0.0.1:7001
  127.0.0.1:7002
  Adding replica 127.0.0.1:7003 to 127.0.0.1:7000
  Adding replica 127.0.0.1:7004 to 127.0.0.1:7001
  Adding replica 127.0.0.1:7005 to 127.0.0.1:7002
  M: 9d8814bf77accad817952f6517b3d8c1203ce1f7 127.0.0.1:7000
     slots:0-5460,8939,12539 (5463 slots) master
  M: ae027e861ca7ea84d216ed9105946b6052a66188 127.0.0.1:7001
     slots:406,5461-10922,12539 (5464 slots) master
  M: 994e4d6b04ff0c51db53fd0592c56befd4aa350c 127.0.0.1:7002
     slots:406,8939,10923-16383 (5463 slots) master
  S: fab6e5074bc0580b8e0d2ed55050939c11c1a51d 127.0.0.1:7003
     replicates 9d8814bf77accad817952f6517b3d8c1203ce1f7
  S: ccb135a97110a34d6dd2422ec31a1f166cb39ff2 127.0.0.1:7004
     replicates ae027e861ca7ea84d216ed9105946b6052a66188
  S: 8499d5e22872064dc54e03b7872a2dd7bf734cd4 127.0.0.1:7005
     replicates 994e4d6b04ff0c51db53fd0592c56befd4aa350c
  Can I set the above configuration? (type 'yes' to accept): yes
  /usr/local/ruby/lib/ruby/gems/2.2.0/gems/redis-3.3.3/lib/redis/client.rb:121:in `call': ERR Slot 406 is already busy (Redis::CommandError)
  	from /usr/local/ruby/lib/ruby/gems/2.2.0/gems/redis-3.3.3/lib/redis.rb:2705:in `block in method_missing'
  	from /usr/local/ruby/lib/ruby/gems/2.2.0/gems/redis-3.3.3/lib/redis.rb:58:in `block in synchronize'
  	from /usr/local/ruby/lib/ruby/2.2.0/monitor.rb:211:in `mon_synchronize'
  	from /usr/local/ruby/lib/ruby/gems/2.2.0/gems/redis-3.3.3/lib/redis.rb:58:in `synchronize'
  	from /usr/local/ruby/lib/ruby/gems/2.2.0/gems/redis-3.3.3/lib/redis.rb:2704:in `method_missing'
  	from ./redis-trib.rb:212:in `flush_node_config'
  	from ./redis-trib.rb:776:in `block in flush_nodes_config'
  	from ./redis-trib.rb:775:in `each'
  	from ./redis-trib.rb:775:in `flush_nodes_config'
  	from ./redis-trib.rb:1296:in `create_cluster_cmd'
  	from ./redis-trib.rb:1700:in `<main>'
  [root@cnlm src]# 
  ```
  - 再次网上搜索一番，发现是原来节点创建集群的时候没有创建成功，删除原节点自动产生的配置文件nodes-700*.conf
  ```bash
  [root@cnlm src]# ./redis-trib.rb  create --replicas 1 172.18.153.216:7000 172.18.153.216:7001 172.18.153.216:7002 172.18.153.216:7003 172.18.153.216:7004 172.18.153.216:7005
  >>> Creating cluster
  >>> Performing hash slots allocation on 6 nodes...
  Using 3 masters:
  127.0.0.1:7000
  127.0.0.1:7001
  127.0.0.1:7002
  Adding replica 127.0.0.1:7003 to 127.0.0.1:7000
  Adding replica 127.0.0.1:7004 to 127.0.0.1:7001
  Adding replica 127.0.0.1:7005 to 127.0.0.1:7002
  M: c62765ab9c35b1d25f51737336f73820d5a9cf11 127.0.0.1:7000
     slots:0-5460 (5461 slots) master
  M: d85db67a717704362c89e7d859b21f2d827972e6 127.0.0.1:7001
     slots:5461-10922 (5462 slots) master
  M: 02df485f52aea1e06d6395eb18d191fdc31ef299 127.0.0.1:7002
     slots:10923-16383 (5461 slots) master
  S: 5b9eeff54051fa69bf87c5d1e02d7adde0b59e87 127.0.0.1:7003
     replicates c62765ab9c35b1d25f51737336f73820d5a9cf11
  S: 4d046e2bf02f0596a92d5a7f124fe8042259a82c 127.0.0.1:7004
     replicates d85db67a717704362c89e7d859b21f2d827972e6
  S: 511c5c88b230423e52c8f3177ae45dc3f158bb78 127.0.0.1:7005
     replicates 02df485f52aea1e06d6395eb18d191fdc31ef299
  Can I set the above configuration? (type 'yes' to accept): yes
  >>> Nodes configuration updated
  >>> Assign a different config epoch to each node
  >>> Sending CLUSTER MEET messages to join the cluster
  Waiting for the cluster to join.....
  >>> Performing Cluster Check (using node 127.0.0.1:7000)
  M: c62765ab9c35b1d25f51737336f73820d5a9cf11 127.0.0.1:7000
     slots:0-5460 (5461 slots) master
     1 additional replica(s)
  S: 4d046e2bf02f0596a92d5a7f124fe8042259a82c 127.0.0.1:7004
     slots: (0 slots) slave
     replicates d85db67a717704362c89e7d859b21f2d827972e6
  M: 02df485f52aea1e06d6395eb18d191fdc31ef299 127.0.0.1:7002
     slots:10923-16383 (5461 slots) master
     1 additional replica(s)
  M: d85db67a717704362c89e7d859b21f2d827972e6 127.0.0.1:7001
     slots:5461-10922 (5462 slots) master
     1 additional replica(s)
  S: 5b9eeff54051fa69bf87c5d1e02d7adde0b59e87 127.0.0.1:7003
     slots: (0 slots) slave
     replicates c62765ab9c35b1d25f51737336f73820d5a9cf11
  S: 511c5c88b230423e52c8f3177ae45dc3f158bb78 127.0.0.1:7005
     slots: (0 slots) slave
     replicates 02df485f52aea1e06d6395eb18d191fdc31ef299
  [OK] All nodes agree about slots configuration.
  >>> Check for open slots...
  >>> Check slots coverage...
  [OK] All 16384 slots covered.
  ```
##3. 到此集群安装配置已完成，接下来验证一下，很遗憾，没有成功存储数据到redis集群中
  ```bash
  [root@cnlm 7000]# redis-cli -h 127.0.0.1 -p 7000
  127.0.0.1:7000> set phone 13880**8929
  (error) MOVED 8939 127.0.0.1:7001
  127.0.0.1:7000> get phone
  (error) MOVED 8939 127.0.0.1:7001
  ```
  - 又搜索了一番，最后确定是客户端没有以启用集群模式的方式连接redis集群，因此不能重定向到其他节点槽（slot），通过在连接的时候指定`-c`参数启用集群模式
  ```bash
  [root@cnlm 7000]# redis-cli -c  -h 127.0.0.1 -p 7000
  127.0.0.1:7000> set phone 138
  -> Redirected to slot [8939] located at 127.0.0.1:7001
  OK
  127.0.0.1:7001> get phone
  "138"
  127.0.0.1:7001> 

  ```

##4. 下期预告，使用jedis操作集群
  

------------------------------------------
    
- 项目地址:[https://github.com/v5zhu/cnlm-blog](https://github.com/v5zhu/cnlm-blog)
- 欢迎加入新QQ群：566654343
- 本人QQ：2810010108