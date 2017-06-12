package com.mia.ciku.download;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


public class HttpClientUtil {


    public static String createPost(String url, Map<String, Object> simpleRequestParamMap) {
        String result = null;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        try {
        	if(simpleRequestParamMap!= null){
        		for (String key : simpleRequestParamMap.keySet()) {
        			Object value = simpleRequestParamMap.get(key);
        			if (value != null) {
        				nvps.add(new BasicNameValuePair(key, value.toString()));
        			}
        		}
        	}
            httppost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            CloseableHttpResponse response = httpclient.execute(httppost);
            result = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
            }
        }
        return result;
    }

    /**
     *
     * @param params 参数
     * @param url    接口地址
     */
    public static String sendPost(String url, List<BasicNameValuePair> params) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        try {
            UrlEncodedFormEntity paramEntity = new UrlEncodedFormEntity(params, "UTF-8");
            httpPost.setEntity(paramEntity);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
        } finally {
            httpPost.abort();
        }
        return null;
    }
}