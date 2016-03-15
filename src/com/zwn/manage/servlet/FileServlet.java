package com.zwn.manage.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.zwn.manage.FileSnapshot;
import com.zwn.manage.FileWalker;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class FileServlet extends HttpServlet {

	private static final long serialVersionUID = -1072813553794564921L;

	private static Logger logger = Logger.getLogger(FileServlet.class);

	private static String[] IMAGE_TYPE = { "bmp", "png", "jpg", "jpeg", "gif" };

	private static Map<String, String> handlerMap = new HashMap<String, String>();

	private static String SUCCESS = "Success";
	
	private static String FAILED = "Failed";
	
	static {
		handlerMap.put("get", "getFileContent");
		handlerMap.put("update", "updateFileContent");
		handlerMap.put("delete", "deleteFile");
		handlerMap.put("create", "createFile");
		handlerMap.put("upload", "uploadFile");
		handlerMap.put("download", "downloadFile");
		handlerMap.put("refresh", "refresh");
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) {
		String methodName = request.getParameter("method");
		String filePath = request.getParameter("filePath");
		String handler = handlerMap.get(methodName);
		try {
			request.setCharacterEncoding("utf-8");
			response.setCharacterEncoding("utf-8");
			logger.info("Call '" + methodName + "' Request " + ", filePath = " + filePath);
			Method method = this.getClass().getMethod(handler,HttpServletRequest.class, HttpServletResponse.class);
			method.invoke(this, request, response);
		} catch (NoSuchMethodException e) {
			logger.error("Fail to get handler for method " + methodName, e);
			e.printStackTrace();
		} catch (SecurityException e) {
			logger.error("Fail to get handler for method " + methodName, e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			logger.error("Fail to excute handler method " + methodName, e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			logger.error("Fail to excute handler method " + methodName, e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			logger.error("Fail to excute handler method " + methodName, e);
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {

		}
	}

	public void updateFileContent(HttpServletRequest request,
			HttpServletResponse response) {
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			String path = request.getParameter("filePath");
			String content = request.getParameter("content");
			File f = new File(path);
			if (isImageFile(f)) {
				writeImageFile(f, content);
			} else {
				writeTextFile(f, content);
			}
			writer.write(SUCCESS);
		} catch (IOException e) {
			logger.error(e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	public void getFileContent(HttpServletRequest request,HttpServletResponse response) {
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			Map<String, String> detail = new HashMap<String, String>();
			String path = request.getParameter("filePath");
			File f = new File(path);
			if (f.exists()) {
				if (isImageFile(f)) {
					detail.put("type", "image");
					detail.put("content", getImageStr(f));
				} else {
					detail.put("type", "text");
					detail.put("content", getTxtStr(f));
				}
				writer.write(JSONObject.fromObject(detail).toString());
			} else {
				writer.write("");
			}
		} catch (IOException e) {
			logger.error(e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	public void deleteFile(HttpServletRequest request,HttpServletResponse response) {
		FileSnapshot filess = (FileSnapshot) getServletContext().getAttribute("root");
		String path = request.getParameter("filePath");
		File f = new File(path);
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			if (!f.delete()) {
				logger.info("Fail to delete '" + f.getAbsolutePath() + "'");
				writer.write(FAILED);
			} else {
				filess.removeByPath(path);
				writer.write(SUCCESS);
			}
		} catch (IOException e) {

		} finally {
			writer.close();
		}
	}

	public void createFile(HttpServletRequest request,HttpServletResponse response) {
		String path = request.getParameter("filePath");
		String type = request.getParameter("type");
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			File f = new File(path);
			if ("folder".equals(type)) {
				f.mkdir();
			} else {
				String content = request.getParameter("content");
				if (isImageFile(f)) {
					writeImageFile(f, content);
				} else {
					writeTextFile(f, content);
				}
			}
			writer.write(SUCCESS);
		} catch (IOException e) {
			logger.error("Failt to create file '" + path + "'", e);
			writer.write(FAILED);
		}
	}

	public void downloadFile(HttpServletRequest request,
			HttpServletResponse response) {
		String path = request.getParameter("filePath");
		response.setContentType("text/plain");
		try {
			File f = new File(path);
			FileInputStream in = new FileInputStream(f);
			response.setHeader("Location", f.getName());
			response.setHeader("Content-Disposition", "attachment; filename=" + f.getName());
			ServletOutputStream outputStream = response.getOutputStream();
			byte[] buffer = new byte[1024];
			int i = -1;
			while ((i = in.read(buffer)) != -1) {
				outputStream.write(buffer, 0, i);
			}
			outputStream.flush();
			outputStream.close();
			in.close();
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error("Failt to find file '" + path + "'", e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Failt to get ServletOutputStream", e);
		}
	}

	public void refresh(HttpServletRequest request, HttpServletResponse response) {
		String path = request.getParameter("filePath");
		FileSnapshot root = (FileSnapshot) request.getServletContext().getAttribute("root");
		FileSnapshot toWrite = null;
		if(StringUtils.isEmpty(path)){
			root.load();
			toWrite = root;
		}else{
			toWrite = root.searchByPath(path);
			toWrite.load();
		}
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			writer.write(JSONObject.fromObject(toWrite).toString());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Failt to get Response Writer", e);
		}
	}

	private void writeTextFile(File file, String cnt) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream out = new FileOutputStream(file);
		out.write(cnt.getBytes("utf-8"));
		out.close();
	}

	private void writeImageFile(File file, String cnt) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream out = new FileOutputStream(file);
		out.write(generateImage(cnt));
		out.flush();
		out.close();
	}

	private boolean isImageFile(File f) {
		return isInTypes(f, IMAGE_TYPE);
	}

	private boolean isInTypes(File f, String[] types) {
		String name = f.getName();
		String[] strs = name.split("\\.");
		String fileType = strs[strs.length - 1];
		for (String s : types) {
			if (s.equals(fileType)) {
				return true;
			}
		}
		return false;
	}

	private String getImageStr(File imgFile) {
		InputStream in = null;
		byte[] data = null;
		String ret = null;
		try {
			in = new FileInputStream(imgFile);
			data = new byte[in.available()];
			in.read(data);
			in.close();
			BASE64Encoder encoder = new BASE64Encoder();
			ret = encoder.encode(data);
		} catch (IOException e) {
			logger.error("Fail to BASE64Encoder " + imgFile.getAbsolutePath(),e);
		}
		return ret;
	}

	private String getTxtStr(File txtFile) {
		InputStream in = null;
		byte[] data = null;
		String ret = null;
		try {
			in = new FileInputStream(txtFile);
			data = new byte[in.available()];
			in.read(data);
			in.close();
			ret = new String(data);
		} catch (IOException e) {
			logger.error("Fail to read " + txtFile.getAbsolutePath(), e);
		}
		return ret;
	}

	// base64字符串转化成图片
	private byte[] generateImage(String imgStr) throws IOException {
		if (imgStr == null)
			return null;
		BASE64Decoder decoder = new BASE64Decoder();
		byte[] b = decoder.decodeBuffer(imgStr);
		for (int i = 0; i < b.length; ++i) {
			if (b[i] < 0) {// 调整异常数据
				b[i] += 256;
			}
		}
		return b;
	}
}
