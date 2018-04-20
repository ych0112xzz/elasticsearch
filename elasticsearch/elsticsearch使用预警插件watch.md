### 安装
elasticsearch 5.x以后watcher插件集成在x-pack包中，无需安装。以下具体内容可参考[官方文档](https://www.elastic.co/guide/en/x-pack/5.5/index.html)

### watcher插件配置使用

#### 配置流程：
- Schedule the watch and define an input：设置定时器和输入源(错误数据的查询条件)
- Add a condition：设置触发条件（condition是否查询到了错误数据）
- Take action：设置触发动作(action发现错误后执行)

#### 配置实例
```json
{
  "trigger" : { "schedule" : { "interval" : "10s" }},#1
  "input" : {#2
    "search" : {
      "request" : {
        "indices" : [ "logs" ],
        "body" : {
          "query" : {
            "match" : { "message": "error" }
          }
        }
      }
    }
  },
  "condition" : {#3
    "compare" : { "ctx.payload.hits.total" : { "gt" : 0 }}
  },
  "actions" : {#4
    "log_error" : {
      "logging" : {
        "text" : "Found {{ctx.payload.hits.total}} errors in the logs"
      }
    }
  }
}

```
具体步骤及功能解释如下：
- #1，#2周期搜索日志文件并把结果装载到watcher，使用schedule和input配置。（示例为每隔10秒钟搜索错误日志）；
- add a condition 设置触发条件（条件为日志错误条数大于0）；
- take action 设置触发动作（以下动作为当错误监测到时把信息写入到Elasticsearch日志中）.

#### API查看
如上例，如果一开始logs索引中没有任何数据，我们将每隔10s得到一条记录，但没有执行action操作。现在我们向索引里插入一条数据，此时condition检测为true，action会执行，watch_record域可以看到相关记录
```json
POST logs/event
{
    "timestamp" : "2015-05-17T18:12:07.613Z",
    "request" : "GET index.html",
    "status_code" : 404,
    "message" : "Error: File not found"
}

```

我们可以通过一下请求来获取watch history里的记录：
```json
GET .watcher-history*/_search?pretty
{
  "query" : {
    "bool" : {
      "must" : [
        { "match" : { "result.condition.met" : true }},
        { "range" : { "result.execution_time" : { "from" : "now-10s" }}}
      ]
    }
  }
}

```

- 注意，由于是测试，trigger的刷新频率较高，在实验结束后可删除该watch
```json
DELETE _xpack/watcher/watch/log_error_watch
```
我们也可以通过`deactivate`api使该watch暂时不可用，等再次使用时用`activate`激活。
```json
Post _xpack/watcher/watch/log_error_watch/_deactivate
Post _xpack/watcher/watch/log_error_watch/_activate
```

### 功能
#### Input
- simple: load static data into the execution context.加载静态数据；
- search: load the results of a search into the execution context.对index、type或document的搜索作为输入源；
- http: load the results of an HTTP request into the execution context.获取一个http请求的相应数据作为后续处理；
- chain: use a series of inputs to load data into the execution context.将一个input的数据作为另一个input的输入源。

#### Actions
Watcher支持的Action类型有很多：Email(邮件),Webhook(第三方对接),Index(索引),Logging(日志记录)，HipChat Action，Slack Action，PagerDuty Action
Jira Action等，这里我们以email action为例说明。

```json
##每10秒检测一次集群状态，如果集群状态错误(red)，则发送邮件给运维
"trigger" : {
    "schedule" : { "interval" : "10s" }
  },
  "input" : {
    "http" : {
      "request" : {
       "host" : "localhost",
       "port" : 9200,
       "path" : "/_cluster/health"
      }
    }
  },
  "condition" : {
    "compare" : {
      "ctx.payload.status" : { "eq" : "red" }
    }
  },
"actions" : {
    "email_admin" : { 
      "email": {
        "to": "'<yourname>@example.com>'",
        "subject": "Error Monitoring Report",
        "attachments" : {##h可选，获取附件
          "error_report.pdf" : {
            "reporting" : {
              "url": "http://0.0.0.0:5601/api/reporting/generate/dashboard/Error-Monitoring?_g=(time:(from:now-1d%2Fd,mode:quick,to:now))", 
              "retries":6, 
              "interval":"1s", 
              "auth":{ ##网站用户认证
                "basic":{
                  "username":"elastic",
                  "password":"changeme"
                }
              }
            }
          }
        }
      }
    }
  }
}

```
使用前必须配置elasticsearch.yml配置文件,每种邮件配置略有不同，具体参考官方文档。下面以163邮箱为例
```json
xpack.notification.email.account:
    outlook_account:
        profile: standard
        email_defaults:
            from: yourname@163.com
        smtp:
            auth: true
            starttls.enable: true
            sstarttls.required: true
            host: smtp.163.com
            port: 25
            user: yourname@@163.com
            password: password
#password是第三方授权码
```




