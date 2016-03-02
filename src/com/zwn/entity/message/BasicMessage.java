package com.zwn.entity.message;

import java.io.Writer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.core.util.QuickWriter;

public class BasicMessage {
	
	public static String TEXT_TYPE = "text";
	
	private String toUserName;
	
	private String fromUserName;
	
	private long createTime;
	
	private String msgType;
	
	private long msgId;
	
	protected static XStream XSTREAM = new XStream(new XppDriver() {
		public HierarchicalStreamWriter createWriter(Writer out) {
			return new PrettyPrintWriter(out) {
				 // 对所有xml 节点的转换都增加CDATA 标记
				 boolean cdata = true;
				 @SuppressWarnings("unchecked")
				 public void startNode(String name, Class clazz) {
					if(!"xml".equals(name)){
						String first = name.substring(0,1);
						name = first.toUpperCase() + name.substring(1);
					}
					super.startNode(name, clazz);
				 }
				 protected void writeText(QuickWriter writer, String text) {
					 if (cdata) {
						 writer.write("<![CDATA[");
						 writer.write(text);
						 writer.write("]]>");
					 } else {
						 writer.write(text);
					 }
				 }
			};
		};
	});	 
	
	public String toXMLString(){
		XSTREAM.alias("xml", this.getClass());
		return XSTREAM.toXML(this);
	}
	
	public static void main(String[] args) {
		BasicMessage m = new BasicMessage();
		System.out.println(m.toXMLString());
	}
	
	public String getToUserName() {
		return toUserName;
	}
	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}
	public String getFromUserName() {
		return fromUserName;
	}
	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
