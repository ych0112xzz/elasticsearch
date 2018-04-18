# ElasticSearch的安装

## 安装elasticsearch
1. 安装JDK1.8；
2. 安装ElasticSearch。以Windows为例，直接下载ElasticSearch5.5，解压后，在bin目录下有elasticsearch.bat文件，双击即可启动es。
3. bin目录下主要是启动es、安装插件等bat文件；config目录下存放es配置相关文件，具体如下。访问localhost:9200验证。
```
config/elasticsearch.yml   主配置文件,对集群名称、host、日志等的配置
config/jvm.options         jvm参数配置文件
cofnig/log4j2.properties   日志配置文件
```

## elasticsearch安装kibana
**Kibana** 是一个设计与 **Elasticsearch** 一起工作的，开放源码的，用于分析以及可视化的平台。您可以使用 **Kibana** 来 **search**（查询），**view**（浏览）并且可以与存储在 **Elasticsearch indices**（索引）中的数据交互。您可以很容易的以各种各样的 **charts**（图表），**tables**（表） 和 **maps**（地图） 样式来针对 **data**（数据）执行高级的 **data analysis**（数据分析）以及 **visualize**（可视化）。关于kibana的使用可以参见官方文档。
1.windows直接下载压缩包解压，启动bin目录下kibana.bat即可，访问`localhost:5601`验证，可以打开kibana界面。

## es和kibana安装插件（以xpack为例）
x-pack是elasticsearch的一个扩展包，将安全，警告，监视，图形和报告功能捆绑在一个易于安装的软件包中

1.kibana
```
bin /kibana-plugin` `install` `<package   name or URL>`
```
如
```
bin /kibana-plugin install x-pack
```
2.es
```
bin/elasticsearch-plugin install x-pack
```
3.如果是离线安装，下载安装包后指定安装包目录即可。
4.更新插件，必须先移除原来插件，运行remove命令
```
bin /kibana-plugin remove x-pack
```
5.禁用插件
您可以在插件的 **package.json** 文件中找到插件的插件 **ID** 作为 **name** 属性的值。
```
/bin/kibana --<plugin ID>.enabled=false # 1
```
6.验证x-pack安装,访问`localhost:5601`验证,需要输入用户名和密码。默认用户名elastic，密码changeme。

# ElasticSearch分布式集群部署研究

## 节点类型
多机集群中的节点可以分为master nodes和data nodes,在配置文件中使用Zen发现(Zen discovery)机制来管理不同节点。Zen发现是ES自带的默认发现机制，使用多播发现其它节点。只要启动一个新的ES节点并设置和**集群相同的名称**这个节点就会被加入到集群中。
- master node：主要用于元数据(metadata)的处理，比如索引的新增、删除、分片分配等。
- data node：节点上保存了数据分片。它负责数据相关操作，比如分片的 CRUD，以及搜索和整合操作。这些操作都比较消耗 CPU、内存和 I/O 资源；
- client node:起到路由请求的作用，实际上可以看做负载均衡器。( 对于没有很多请求的业务，client node可以不加，master和data足矣)。
- 协调节点：协调节点，是一种角色，而不是真实的Elasticsearch的节点，你没有办法通过配置项来配置哪个节点为协调节点。**集群中的任何节点，都可以充当协调节点的角色**。当一个节点A收到用户的查询请求后，会把查询子句分发到其它的节点，然后合并各个节点返回的查询结果，最后返回一个完整的数据集给用户。在这个过程中，节点A扮演的就是协调节点的角色。毫无疑问，协调节点会对CPU、Memory要求比较高。

**master节点也可作为data节点，但是数据节点的负载较重，所以需要考虑将二者分离开，设置专用的数据节点，避免因数据节点负载重导致主节点不响应。**

## 集群部署
以三节点为例，说明部署elasticsearch集群的步骤
1.按上一节步骤在每台机器上安装es；
2.配置各节点的`elasticsearch.yml`即可。
--- 以下为master节点的配置文件，关键要配置，集群名称，节点名称，以及`discovery.zen.ping.unicast.hosts`属性（各节点的ip或hostname）。
```
# ======================== Elasticsearch Configuration =========================
# ---------------------------------- Cluster -----------------------------------
#
# Use a descriptive name for your cluster:
# 集群名称，分布式部署，确保该名称唯一。
cluster.name: elasticsearch
#
# ------------------------------------ Node ------------------------------------
#
# Use a descriptive name for the node:
# 节点名称
node.name: node-1
node.master: true
node.data: true
#
# Add custom attributes to the node:
#
# node.rack: r1
#
# ----------------------------------- Paths ------------------------------------
# 数据存储
# Path to directory where to store the data (separate multiple locations by comma):
#
path.data: /data/elasticsearch/data
path.logs: /data/logs/elasticsearch
path.plugins: /data/elasticsearch/plugins
path.scripts: /data/elasticsearch/scripts
#
# Path to log files:
#
# path.logs: /path/to/logs
#
# ----------------------------------- Memory -----------------------------------
#
# Lock the memory on startup:
#
bootstrap.mlockall: true
#
# Make sure that the `ES_HEAP_SIZE` environment variable is set to about half the memory
# available on the system and that the owner of the process is allowed to use this limit.
#
# Elasticsearch performs poorly when the system is swapping the memory.
#
# ---------------------------------- Network -----------------------------------
#
# Set the bind address to a specific IP (IPv4 or IPv6):
# IP地址
network.host: 110.10.11.130
#
# Set a custom port for HTTP:
# 端口
http.port: 9200

# --------------------------------- Discovery ----------------------------------
#
# Pass an initial list of hosts to perform discovery when new node is started:
# The default list of hosts is ["127.0.0.1", "[::1]"]
discovery.zen.ping.unicast.hosts: [" 110.10.11.130 :9300", "10.118.110.112:9300", "110.0.11.143:9300"]
# Prevent the "split brain" by configuring the majority of nodes (total number of nodes / 2 + 1):
#
# discovery.zen.minimum_master_nodes: 3
#
discovery.zen.minimum_master_nodes: 2
# For more information, see the documentation at:
# ---------------------------------- Gateway -----------------------------------
#
# Block initial recovery after a full cluster restart until N nodes are started:
#
# gateway.recover_after_nodes: 3
gateway.recover_after_nodes: 3
gateway.recover_after_time: 5m
gateway.expected_nodes: 1
#
# ---------------------------------- Various -----------------------------------
#
# Disable starting multiple nodes on a single system:
#
# node.max_local_storage_nodes: 1
#
# Require explicit names when deleting indices:
#
# action.destructive_requires_name: true
#index.analysis.analyzer.ik.type : “ik”
script.engine.groovy.inline.search: on
script.engine.groovy.inline.aggs: on
indices.recovery.max_bytes_per_sec: 100mb
indices.recovery.concurrent_streams: 10
```
--- 对于data节点(其余与master节点一致)
```
node.name: laoyng03
node.master: false
node.data: true
network.host: 110.0.11.143
```
3.运行三个节点上的elasticsearch服务即可（顺序无关）。

## 单机多节点集群部署
如果想要在一台机器上启动多个节点，步骤如下：

1.复制一份ELasticsearch的安装包
2.修改端口，比如一个是9200，一个是9205
3.删除data目录下的数据(如果是新解压的安装包就不必了)。

# java API
- 声明client
```
TransportClient client = TransportClient.builder().build()
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host1"), 9300))
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host2"), 9300));
```

- index,get,delete,update(prepare)
- searchapi(QueryBuilders)
- QueryBuilders(match,term)

# 相关问题

## bulk换行符原理
Elasticsearch可以直接读取被网络缓冲区接收的原始数据。 它使用换行符字符来识别和解析小的 `action/metadata` 行来决定哪个分片应该处理每个请求。
