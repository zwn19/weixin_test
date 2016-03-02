package com.zwn.manage.servlet;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.zwn.entity.menu.ClickButton;
import com.zwn.entity.menu.ViewButton;
import com.zwn.entity.menu.WrapButton;
import com.zwn.utils.HttpClientUtil;
import com.zwn.utils.Utils;

public class InitServlet extends HttpServlet {

	private static final long serialVersionUID = 5471270863961L;

	private static Logger logger = Logger.getLogger(InitServlet.class);

	private static String createMenuUrl = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token="+ Utils.getAccessToken();
	
	public void init(ServletConfig config) throws ServletException {
		setLogFilePath(config);
		//createMenu();
	}
	private void setLogFilePath(ServletConfig config){
		Properties props = new Properties();
		try {
			props.load(InitServlet.class.getClassLoader().getResourceAsStream("log4j.properties"));
			String cxtPath = config.getServletContext().getRealPath("/");// 设置路径
			Pattern p = Pattern.compile("log4j\\.appender\\.\\w+\\.File");
			Matcher matcher = null;
			for(Object s : props.keySet()){
				String prop = s.toString();
				matcher = p.matcher(prop);
				if(matcher.matches()){
					String newPath = cxtPath + props.getProperty(prop);
					props.setProperty(prop, newPath);
					logger.info("set '"+ prop + "' as value '" + newPath + "' in log4j.properties");
				}
			}
			PropertyConfigurator.configure(props);// 装入log4j配置信息
		} catch (IOException e) {
			logger.error("Fail to set log4j.appender.file.File", e);
		}
	}
	private void createMenu(){
		WrapButton w = new WrapButton();
		ClickButton cb1 = new ClickButton();
		cb1.setKey("V1001_Tool");
		cb1.setName("工具");
		
		ClickButton cb2 = new ClickButton();
		cb2.setKey("V1001_VIEW_BUTTON");
		cb2.setName("网站");
		
		ClickButton cb3 = new ClickButton();
		cb3.setKey("V1001_HELP");
		cb3.setName("帮助");
		
		w.addButton(cb1);
		w.addButton(cb2);
		w.addButton(cb3);
		
		ViewButton vb1 = new ViewButton();
		vb1.setName("百度");
		vb1.setUrl("https://www.baidu.com/");
		ViewButton vb2 = new ViewButton();
		vb2.setName("Google");
		vb2.setUrl("https://www.google.com");
		
		cb2.addButton(vb1);
		cb2.addButton(vb2);
		if(logger.isDebugEnabled()){
			logger.debug("Call Create MenuUrl:" + createMenuUrl);
			logger.debug("Menu json : " + JSONObject.fromObject(w));
		}
		System.out.println(w.toJsonString());
		String createRet = HttpClientUtil.doPostJson(createMenuUrl,JSONObject.fromObject(w.toJsonString()));
		JSONObject json = JSONObject.fromObject(createRet);
		if(json.getInt("errcode") != 0){
			logger.error("Fail To Create Menu");
		}
		if(logger.isDebugEnabled()){
			logger.debug(createRet);
		}
	}
	
	public static void main(String[] args) {
		InitServlet o = new InitServlet();
		o.createMenu();
		o.destroy();
	}
}
