package com.zwn.manage.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.zwn.manage.FileSnapshot;
import com.zwn.manage.FileWalker;

public class ManageServlet extends HttpServlet {
	
	private static final long serialVersionUID = -1072813553794564921L;
	
	private static Logger logger = Logger.getLogger(ManageServlet.class);
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		String path = config.getServletContext().getRealPath("/");
		config.getServletContext().setAttribute("root", getRootFolder(path));
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response){
		PrintWriter writer = null;
		try {
			request.setCharacterEncoding("utf-8");
			response.setCharacterEncoding("utf-8");
			String refresh = request.getParameter("refresh");
			ServletContext servletContext = request.getServletContext();
			if(Boolean.parseBoolean(refresh)){
				String path = servletContext.getRealPath("/");
				servletContext.setAttribute("root", getRootFolder(path));
			}
			Object root = servletContext.getAttribute("root");
			writer = response.getWriter();
			writer.write(JSONObject.fromObject(root).toString());
		} catch (IOException e) {
			logger.error(e);
		}finally{
			if(writer != null){
				writer.close();
			}
		}
	}
	
	private FileSnapshot getRootFolder(String path){
		FileWalker fileWalker = new FileWalker(path);
		FileSnapshot fileTree = fileWalker.getRoot();
		return fileTree;
	}
}
