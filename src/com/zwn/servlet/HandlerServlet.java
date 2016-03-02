package com.zwn.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.zwn.constant.Constant;
import com.zwn.entity.message.BasicMessage;
import com.zwn.entity.message.TextMessage;
import com.zwn.utils.AesException;
import com.zwn.utils.Configure;
import com.zwn.utils.Utils;
import com.zwn.utils.WXBizMsgCrypt;

public class HandlerServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5479202367170863961L;

	private static Logger logger = Logger.getLogger(HandlerServlet.class);

	private static WXBizMsgCrypt crypt = null;
	
	static{
		try {
			crypt = new WXBizMsgCrypt(Constant.TOCKEN, Constant.EncodingAESKey, Constant.APP_ID);
		} catch (AesException e) {
			logger.error("Fail to init WXBizMsgCrypt",e);
		}
	}
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			request.setCharacterEncoding("UTF-8");
			response.setCharacterEncoding("UTF-8");
			String echostr = request.getParameter("echostr");
			logger.debug("GET : checkSignature = "+checkSignature(request));
			if (!Configure.getBoolean("checkSignature") || checkSignature(request)) {
				writer.write(echostr + "");
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("GET-IOException", e);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			logger.error("GET-NoSuchAlgorithmException", e);
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("GET-Exception", e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response){
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			request.setCharacterEncoding("UTF-8");
			response.setCharacterEncoding("UTF-8");
			logger.debug("POST : checkSignature = "+checkSignature(request));
			if (!Configure.getBoolean("checkSignature") || checkSignature(request)) {
				Map<String, String> map = parseXml(request);
				String content = map.get("Content");
				TextMessage msg = new TextMessage();
				msg.setContent(content);
				msg.setToUserName(map.get("FromUserName"));
				msg.setFromUserName(map.get("ToUserName"));
				msg.setMsgId(Long.valueOf((map.get("MsgId"))));
				msg.setCreateTime(Long.valueOf((map.get("CreateTime"))));
				msg.setMsgType(BasicMessage.TEXT_TYPE);
				if(logger.isDebugEnabled()){
					logger.debug("(orgin)response : " + msg.toXMLString());
				}
				String timestamp = request.getParameter("timestamp");
				String nonce = request.getParameter("nonce");
				String cnt = crypt.encryptMsg(msg.toXMLString(), timestamp, nonce);
				writer.write(cnt);
				if(logger.isDebugEnabled()){
					logger.debug("(encoded)response : " + cnt);
				}
			}else{
				
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("POST-IOException", e);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			logger.error("POST-NoSuchAlgorithmException", e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("POST-Exception", e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private boolean checkSignature(HttpServletRequest request) throws NoSuchAlgorithmException {
		// 微信加密签名
		String signature = request.getParameter("signature");
		// 时间戳
		String timestamp = request.getParameter("timestamp");
		// 随机数
		String nonce = request.getParameter("nonce");
		// 随机字符串
		return Utils.checkSignature(Constant.TOCKEN, signature,timestamp, nonce);
	}
	
	private Map<String, String> parseXml(HttpServletRequest request)
			throws Exception {
		// 将解析结果存储在HashMap 中
		Map<String, String> map = new HashMap<String, String>();
		String timestamp = request.getParameter("timestamp");
		String nonce = request.getParameter("nonce");
		String msg_signature = request.getParameter("msg_signature");
		
		// 从request 中取得输入流
		InputStream inputStream = request.getInputStream();
		String xml = getXML(inputStream);
		if(logger.isDebugEnabled()){
			logger.debug("(orgin)get request : " + xml);
		}
		xml = crypt.decryptMsg(msg_signature, timestamp, nonce, xml);
		if(logger.isDebugEnabled()){
			logger.debug("(decoded)get request : " + xml);
		}
		// 读取输入流
		SAXReader reader = new SAXReader();
		Document document = reader.read(new ByteArrayInputStream(xml.getBytes("utf-8")));
		// 得到xml 根元素
		Element root = document.getRootElement();
		// 得到根元素的所有子节点
		List<Element> elementList = root.elements();

		// 遍历所有子节点
		for (Element e : elementList) {
			map.put(e.getName(), e.getText());
		}
		// 释放资源
		inputStream.close();
		inputStream = null;
		return map;
	}
	
	private String getXML(InputStream inputStream) throws IOException{
		StringBuilder b = new StringBuilder();
		int len = -1;
		byte[] bs = new byte[1024];
		while((len = inputStream.read(bs)) > -1){
			b.append(new String(bs,0,len));
		}
		return b.toString();
	}

	public static void main(String[] args) {
		
	}

}
