package com.htffund.yangcheng.elasticsearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class ESClient {


     static TransportClient client;
    private static Logger logger = LogManager.getLogger(ESClient.class);

    public static void main(String[] args) {
        ESClient esClient = new ESClient();
        esClient.startup();

    }


    public void startup() {
        Settings settings = Settings.builder()
                .put("cluster.name", "elasticsearch").put("xpack.security.transport.ssl.enabled", false).put("xpack.security.user", "elastic:changeme").build();
        client = new PreBuiltXPackTransportClient(settings);
        try {
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
            logger.info("connect success!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("failed");
            logger.error("can't connect to the es");
            System.exit(1);
        }
        //  .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host2"), 9300));
    }


    public void close(){
        client.close();

    }

    public TransportClient getClient() {
        return client;
    }



}
