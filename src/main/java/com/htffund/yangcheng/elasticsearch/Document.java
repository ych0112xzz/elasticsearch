package com.htffund.yangcheng.elasticsearch;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.htffund.yangcheng.elasticsearch.ESClient.client;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class Document {
    private static Logger logger = LogManager.getLogger(Document.class);
    /**
     * 索引
     * 处理json字符串
     * 使用ElasticSearch 帮助类
     */
    public XContentBuilder CreateXContentBuilder() {
        XContentBuilder builder = null;
        try {
            builder = jsonBuilder().startObject().field("email", "1149851790@qq.com")
                    .field("name", "yangcheng").field("username", "ych0112xzz").endObject();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder;
    }

    /**
     * * 索引
     * 处理json字符串
     * 使用集合
     */
    public Map<String, String> CreateList() {
        Map<String, String> jsonMap = new HashMap<String, String>();
        jsonMap.put("email", "1149851790@qq.com");
        jsonMap.put("name", "yangcheng");
        jsonMap.put("username", "ych1112xzz");
        return jsonMap;
    }

    /*
     *index
     */
    public IndexResponse getIndex() {
        Map<String, String> jsonMap = CreateList();
        IndexResponse indexResponse = client.prepareIndex("us", "user", "1002").setSource(jsonMap).get();
        System.out.println(indexResponse.toString());
        return indexResponse;
    }

    /**
     * get
     */
    public GetResponse getGetResponse(String index, String type, String id) {
        GetResponse getResponse = client.prepareGet(index, type, id).get();
        System.out.println(getResponse.getIndex());
        return getResponse;
    }

    public DeleteResponse getDeleteResponse() {
        DeleteResponse deleteResponse = client.prepareDelete("us", "user", "1002").get();
        logger.info("delete success");
        //System.out.println("delete success");
        return deleteResponse;
    }

    /**
     * Bulk
     */
    public BulkResponse getBulkResponse() {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        BulkResponse bulkResponse = null;
        try {
            bulkRequest.add(client.prepareIndex("twitter", "tweet", "1")
                    .setSource(jsonBuilder()
                            .startObject()
                            .field("user", "kimchy")
                            .field("postDate", new Date())
                            .field("message", "trying out Elasticsearch")
                            .endObject()
                    )
            );

            bulkRequest.add(client.prepareIndex("twitter", "tweet", "2")
                    .setSource(jsonBuilder()
                            .startObject()
                            .field("user", "kimchy")
                            .field("postDate", new Date())
                            .field("message", "another post")
                            .endObject()
                    )
            );

            bulkRequest.add(client.prepareIndex("twitter", "tweet", "3")
                    .setSource(jsonBuilder()
                            .startObject()
                            .field("user", "ych")
                            .field("postDate", new Date())
                            .field("message", "another post")
                            .endObject()
                    )
            );
            bulkResponse= bulkRequest.get();
            System.out.println(bulkResponse.toString());

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("something has error!!!");
        }
        return bulkResponse;
    }

    public MultiGetResponse getMultiGetResponse(){
        MultiGetResponse multiGetItemResponses  = client.prepareMultiGet()
                .add("twitter", "tweet", "1")
                .add("twitter", "tweet", "2", "3", "4")
                .add("another", "type", "foo")
                .get();

        for (MultiGetItemResponse itemResponse : multiGetItemResponses) {
            GetResponse response = itemResponse.getResponse();
            if (response.isExists()) {
                String json = response.getSourceAsString();
            }
        }
        return multiGetItemResponses;
    }

}
