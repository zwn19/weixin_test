package com.zwn.manage.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.zwn.manage.FileSnapshot;
import com.zwn.manage.FileWalker;

public class UploadServlet extends HttpServlet {
	
	private static final long serialVersionUID = -1072813553794564921L;
	
	private static Logger logger = Logger.getLogger(UploadServlet.class);
	
	private static String[] IMAGE_TYPE = {"bmp","png","jpg","jpeg","gif"};
	
	private static String[] TEXT_TYPE = {"txt","css","js","properties","html","xml","jsp"};
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response){
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			logger.error(e);
		}finally{
			if(writer != null){
				writer.close();
			}
		}
	}
	
}
