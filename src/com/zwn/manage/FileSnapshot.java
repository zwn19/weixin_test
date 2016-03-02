package com.zwn.manage;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class FileSnapshot implements Comparable<FileSnapshot>
{
	private String name;
	
	private String fullName;
	
	private boolean isFolder;
	
	private List<FileSnapshot> children;
	
	private Map<String,FileSnapshot> childrenMap = new HashMap<String,FileSnapshot>();

	public String getName() 
	{
		return name;
	}

	public void setName(String name) 
	{
		this.name = name;
	}

	public String getFullName() 
	{
		return fullName;
	}

	public void setFullName(String fullName) 
	{
		this.fullName = fullName;
	}

	public boolean isFolder() 
	{
		return isFolder;
	}

	public void setFolder(boolean isFolder) 
	{
		this.isFolder = isFolder;
	}

	public FileSnapshot searchByPath(String path)
	{
		path = path.replaceAll("\\\\", "/");
		String[] paths = path.split("/");
		FileSnapshot current = this;
		for(String s : paths)
		{
			if(current == null)
			{
				return null;
			}
			current = current.childrenMap.get(s);
		}
		return current;
	}
	
	public FileSnapshot(File file)
	{
		if(file != null)
		{
			fullName = file.getAbsolutePath();
			name = file.getName();
			isFolder = file.isDirectory();
		}
	}

	public FileSnapshot() {
		
	}

	@Override
	public int compareTo(FileSnapshot o) 
	{
		if(isFolder == o.isFolder())
		{
			return fullName.compareTo(o.getFullName());
		}
		else
		{
			if(isFolder)
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
	}

	public List<FileSnapshot> getChildren() {
		return children;
	}

	public void setChildren(List<FileSnapshot> children) {
		this.children = children;
		if(children != null && !children.isEmpty())
		{
			for(FileSnapshot fsn : children)
			{
				String name2 = fsn.getName();
				childrenMap.put(StringUtils.isEmpty(name2) ? fsn.getFullName().replace("\\","/").replace("/",""): name2, fsn);
			}
		}
	}
}
