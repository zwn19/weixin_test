package com.zwn.manage;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class FileSnapshot implements Comparable<FileSnapshot> {
	
	public static String getParentPath(String path){
		Pattern p = Pattern.compile("(.+)/[^/\\:]+");
		Matcher matcher = p.matcher(path);
		String parentPath = path;
		if (matcher.find()) {
			parentPath = matcher.group(1);
		}
		return parentPath;
	}
	
	private String name;

	private String fullName;

	private boolean isFolder;

	private List<FileSnapshot> children;

	private Map<String, FileSnapshot> childrenMap = new HashMap<String, FileSnapshot>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public boolean isFolder() {
		return isFolder;
	}

	public void setFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}

	public FileSnapshot searchByPath(String path) {
		if(fullName.equals(path)){
			return this;
		}
		if(!isFolder){
			return null;
		}
		if(this.childrenMap == null || this.children == null || this.children.isEmpty()){
			return null;
		}
		if(this.childrenMap.containsKey(path)){
			return this.childrenMap.get(path);
		}
		FileSnapshot result = null;
		for(FileSnapshot f : this.children){
			result = f.searchByPath(path);
			if(result != null){
				return result;
			}
		}
		return null;
	}

	public void removeByPath(String path){
		String parentPath = FileSnapshot.getParentPath(path);
		FileSnapshot parent = this.searchByPath(parentPath);
		FileSnapshot sub = parent.searchByPath(path);
		parent.removeFileSnapshot(sub);
	}
	
	public boolean removeFileSnapshot(FileSnapshot sub) {
		if (this.children.indexOf(sub) > -1) {
			this.children.remove(sub);
			String name2 = sub.getName();
			name2 = StringUtils.isEmpty(name2) ? "" : name2;
			this.childrenMap.remove(name2);
			return true;
		}else{
			for(FileSnapshot f : this.children){
				if(f.removeFileSnapshot(sub)){
					return true;
				}
			}
			return false;
		}
	}
	
	public boolean replaceSub(FileSnapshot sub) {
		FileSnapshot toBeReplaced = this.childrenMap.get(sub.getFullName());
		if (toBeReplaced != null) {
			int index = this.children.indexOf(toBeReplaced);
			this.children.set(index, sub);
			this.childrenMap.put(sub.getFullName(),sub);
			return true;
		}else{
			for(FileSnapshot f : this.children){
				if(f.replaceSub(sub)){
					return true;
				}
			}
			return false;
		}
	}

	public FileSnapshot(File file) {
		if (file != null) {
			fullName = file.getAbsolutePath().replaceAll("\\\\", "/");
			name = file.getName();
			isFolder = file.isDirectory();
		}
	}

	public FileSnapshot() {

	}

	@Override
	public int compareTo(FileSnapshot o) {
		if (isFolder == o.isFolder()) {
			return fullName.compareTo(o.getFullName());
		} else {
			if (isFolder) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	public List<FileSnapshot> getChildren() {
		return children;
	}

	public void setChildren(List<FileSnapshot> children) {
		this.children = children;
		if (children != null && !children.isEmpty()) {
			for (FileSnapshot fsn : children) {
				String name2 = fsn.getFullName();
				childrenMap.put(StringUtils.isEmpty(name2) ? "" : name2, fsn);
			}
		}
	}

	public void sortChildren() {
		if (children != null && !children.isEmpty()) {
			Collections.sort(children);
			for (FileSnapshot f : children) {
				f.sortChildren();
			}
		}
	}

	public String getParentPath() {
		return FileSnapshot.getParentPath(fullName);
	}
	
	public void load(){
		FileWalker fileWalker = new FileWalker(fullName);
		FileSnapshot fss = fileWalker.getRoot();
		this.setChildren(fss.getChildren());
	}
	
}
