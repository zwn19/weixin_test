package com.zwn.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/*
 * 利用HttpClient进行post请求的工具类
 */
public class HttpClientUtil {
	private static Logger logger = Logger.getLogger(HttpClientUtil.class);
	
	public static String doGet(String url) throws Exception{
		HttpClient httpClient = new SSLClient();
		HttpGet httpGet= new HttpGet(url);
		HttpResponse response = httpClient.execute(httpGet);
		return responseToString(response,"utf-8");
	}
	public static String doPostJson(String url,JSONObject json){
		return doPostJson(url, json, "UTF-8");
	}
	public static String doPostJson(String url,JSONObject json,String charset) {
		String ret = null;
		//创建连接
        URL urlObject;
		try {
			urlObject = new URL(url);
			HttpURLConnection connection = (HttpURLConnection)urlObject.openConnection();
	        connection.setDoOutput(true);
	        connection.setDoInput(true);
	        connection.setRequestMethod("POST");
	        connection.setUseCaches(false);
	        connection.setInstanceFollowRedirects(true);
	        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
	        connection.connect();
	        //POST请求
	        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
	        out.writeBytes(json.toString());
	        out.flush();
	        out.close();
	        String lines;
	        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        StringBuilder b = new StringBuilder();
	        while ((lines = reader.readLine()) != null) {
	            lines = new String(lines.getBytes(), "utf-8");
	            b.append(lines);
	        }
	        reader.close();
	        // 断开连接
	        connection.disconnect();
	        ret = b.toString();
		} catch (MalformedURLException e) {
			logger.error("Fail to post url",e);
		} catch (IOException e) {
			logger.error("Fail to post url",e);
		}
		return ret;
	}
	
	public static String doPost(String url, Map<String, String> map) throws Exception {
		return doPost(url, map, "UTF-8");
	}

	public static String doPost(String url, Map<String, String> map, String charset) throws Exception {
		HttpClient httpClient = new SSLClient();
		HttpPost httpPost = new HttpPost(url);
		// 设置参数
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> elem = iterator.next();
			list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
		}
		if (list.size() > 0) {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);
			httpPost.setEntity(entity);
		}
		HttpResponse response = httpClient.execute(httpPost);
		return responseToString(response,charset);
	}
	
	private static String responseToString(HttpResponse response,String charset){
		if (response != null) {
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				try {
					return EntityUtils.toString(resEntity, charset);
				} catch (ParseException | IOException e) {
					logger.error("Fail to parse response",e);
				}
			}
		}
		return null;
	}
	static class SSLClient extends DefaultHttpClient {
		public SSLClient() throws Exception {
			super();
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] chain,String authType) throws CertificateException {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain,String authType) throws CertificateException {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = this.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", 443, ssf));
		}
	}
	public static void main(String[] args) {
		
	}
}