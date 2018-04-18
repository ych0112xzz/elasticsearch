package com.htffund.yangcheng.elasticsearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.transport.TransportClient;


public class TestMain {
    private static Logger logger = LogManager.getLogger(TestMain.class);

    public static void main(String[] args) {
        ESClient esClient=new ESClient();
        esClient.startup();
        Document document = new Document();
        //document.getIndex();
        /*document.getGetResponse("us","user","1000");
        document.getDeleteResponse();*/
    }
}
