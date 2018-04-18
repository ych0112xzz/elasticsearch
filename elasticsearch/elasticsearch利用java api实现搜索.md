### 基本查询
- 查询的时候，需要建立一个SearchRequestBuilder，这里面将给出对于哪一个index或者type进行查询，并且所有的设置都可以在这里面进行实现，例如模糊查询，范围查询，前缀查询等.
```$xslt
SearchRequestBuilder responsebuilder = client.prepareSearch("index").setTypes("type")
```
- 上述代码的意思是对于index的type进行查询，其中client即使得到的建立链接，下一步就是要将查询词给进去
```$xslt
SearchResponse myresponse=responsebuilder.setQuery(QueryBuilders.matchPhraseQuery("title", "molong1208 blog"))  
.setFrom(0).setSize(10).setExplain(true).execute().actionGet(); 
```
- 展示
```$xslt
SearchHits hits = myresponse.getHits();  
for (int i = 0; i < hits.getHits().length; i++) {  
           System.out.println(hits.getHits()[i].getSourceAsString());}  
```

### 查询

#### 基本查询

```
responsebuilder.setQuery(QueryBuilders.matchPhraseQuery("title", "molong1208 blog"))
```  
所使用的是matchPhraseQuery(field,text)函数，这个函数的参数有两个,其中对应text的部分是要解析的，例如，molong1208 blog 可能经过解析之后会解析成molong1208 以及blog然后再进行查询的

#### 多词条查询
```$xslt
responsebuilder.setQuery(QueryBuilders.termsQuery("title", "molong1208","blog","csdn"))  
```
对于三个词molong1208，blog，csdn在title字段进行查询，如果有三者中的任意一个即算匹配

#### match_all查询
```
responsebuilder.setQuery(QueryBuilders.matchAllQuery()) 
``` 
#### 常用词查询
```$xslt
responsebuilder.setQuery(QueryBuilders.commonTermsQuery("name", "lishici"))  
```
可以在后面设置具体的cutoffFrequency

#### multi_match查询
```$xslt
responsebuilder.setQuery(QueryBuilders.multiMatchQuery("lishi", "subcat","name"))  
```
multiMatchQuery(text,fields)其中的fields是字段的名字，可以写好几个，每一个中间用逗号分隔



#### simple_query_string查询
```$xslt
responsebuilder.setQuery(QueryBuilders.simpleQueryStringQuery(""))
```
#### 前缀查询
```$xslt
responsebuilder.setQuery(QueryBuilders.prefixQuery("title", "mo"))
```

前一个参数为使用的field后一个参数为所使用的前缀词
#### 通配符查询
```$xslt
responsebuilder.setQuery(QueryBuilders.wildcardQuery("title", "molo?g"))
```
  
#### more_like_this，more_like_this_field
```$xslt
responsebuilder.setQuery(QueryBuilders.moreLikeThisQuery().addLikeText("long"))  
responsebuilder.setQuery(QueryBuilders.moreLikeThisQuery("long"))  
```
有两个方法，其中第二个是在_all范围内进行查询，第一个后面还有很多可以设置，有需要用的可以具体参考
#### rang查询
```$xslt
responsebuilder.setQuery(QueryBuilders.rangeQuery("age").gt(10).lt(20))
```
对于某一个field，大于多少，小于多少

#### dismax查询
```$xslt
responsebuilder.setQuery(QueryBuilders.disMaxQuery().add(QueryBuilders.termQuery("title", "molong1208")))
```
  
#### 正则表达式查询
```$xslt
responsebuilder.setQuery(QueryBuilders.regexpQuery(field, regexp))
```

### bool查询
 上述只是大概给出了具体的查询方式，有些时候可能我们所想要的为比较复杂的查询，例如想要查一个在某个字段必须有某个值，并且另一个字段必须有另外一个值的情况，这种时候就可以使用bool查询，例如下所示
 ```$xslt
responsebuilder.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.multiMatchQuery(query, "name","title","title_1")).must(QueryBuilders.multiMatchQuery(query2, "title2","title3")))
```
 上述的意思是，在title或者title_1或者name字段有query，并且在title2或者title3字段有query2的结果召回来
 当然，可以根据自己的情况，有should，must_not等选择.

 



