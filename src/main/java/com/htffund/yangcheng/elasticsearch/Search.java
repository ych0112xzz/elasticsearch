package com.htffund.yangcheng.elasticsearch;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import static com.htffund.yangcheng.elasticsearch.ESClient.client;

public class Search {
    /**
     * bulk+delete+query
     */
    public BulkResponse bulkDeleteQuery(String index, String type, String key, String value) {

        //构建SearchResponse
        /*SearchResponse response = client.prepareSearch("index1", "index2")
                .setTypes("type1", "type2")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.termQuery("multi", "test"))                 // Query
                .setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
                .setFrom(0).setSize(60).setExplain(true)
                .get();*/
        BulkResponse bulkResponse = null;
        TermQueryBuilder queryBuilder = QueryBuilders.termQuery(key, value);
        SearchResponse scrollResp = client.prepareSearch(index)
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setQuery(queryBuilder)
                .setSize(100).execute().actionGet(); //100 hits per shard will be returned for each scroll

        BulkRequestBuilder bulkRequest = client.prepareBulk();

        while (true) {
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                bulkRequest.add(client.prepareDelete(hit.getIndex(),hit.getType(),hit.getId()));
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).get();
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }
        bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            BulkItemResponse[] bulkItemResponse = bulkResponse.getItems();
            for (int i = 0; i <bulkItemResponse.length ; i++) {
                System.out.println(bulkItemResponse[i].getItemId()+":"+bulkItemResponse[i].getIndex()+":"+bulkItemResponse[i].getFailureMessage());
            }
        }

        return bulkResponse;

    }


    public MultiSearchResponse getMultiSearchResponse() {
        SearchRequestBuilder srb1 = client
                .prepareSearch().setQuery(QueryBuilders.queryStringQuery("elasticsearch")).setSize(1);
        SearchRequestBuilder srb2 = client
                .prepareSearch().setQuery(QueryBuilders.matchQuery("name", "kimchy")).setSize(1);

        MultiSearchResponse multiSearchResponse = client.prepareMultiSearch()
                .add(srb1)
                .add(srb2)
                .execute().actionGet();

        // You will get all individual responses from MultiSearchResponse#getResponses()
        long nbHits = 0;
        for(MultiSearchResponse.Item item:multiSearchResponse.getResponses()){
            SearchResponse response = item.getResponse();
            nbHits += response.getHits().getTotalHits();
        }
        return multiSearchResponse;
    }
}
