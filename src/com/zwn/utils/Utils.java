package com.zwn.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

import org.apache.log4j.Logger;

import com.zwn.constant.Constant;
import com.zwn.servlet.HandlerServlet;

public class Utils {
	
	private static Logger logger = Logger.getLogger(Utils.class);
	
	private static String accessToken = null;
	
	private static String accessToken_url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+ Constant.APP_ID +"&secret=" + Constant.APP_SECRET;
	
	static{
		TokenTimerTask t = new TokenTimerTask();
		t.run();
	}
	
	public static boolean checkSignature(String token,String signature, String timestamp,String nonce) throws NoSuchAlgorithmException{
		String[]  arr = new String[]{token,timestamp,nonce};
		// 将token、timestamp、nonce 三个参数进行字典序排序
		Arrays.sort(arr);
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			content.append(arr[i]);
		}
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] byts = md.digest(content.toString().getBytes());
		return signature.toUpperCase().equals(byteArrToStr(byts));
	}
	
	public static String getAccessToken(){
		return accessToken;
	}
	
	private static String byteArrToStr(byte[] byteArray) {
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < byteArray.length; i++) {
			content.append(byteToHexStr(byteArray[i]));
		}
		return content.toString();
	}
	
	private static String byteToHexStr(byte mByte) {
		 char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		 char[] tempArr = new char[2];
		 tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
		 tempArr[1] = Digit[mByte & 0X0F];
		 String s = new String(tempArr);
		 return s;
	}
	
	private static class TokenTimerTask extends TimerTask{
		@Override
		public void run() {
			int deltaSeconds = 10;
			try {
				String res = HttpClientUtil.doGet(accessToken_url);
				JSONObject json = JSONObject.fromObject(res);
				accessToken = json.getString("access_token");
				String delay = json.getString("expires_in");
				Timer timer = new Timer();
				timer.schedule(new TokenTimerTask(), (Long.parseLong(delay) - deltaSeconds) * 1000);
			} catch (Exception e) {
				logger.error("Fail to get access token", e);
			}
		}
	}
	
}
