package com.zwn.manage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

public class FileWalker {
	
	private FileSnapshot fileTree = new FileSnapshot();
	
	private File[] files;
	
	public FileWalker() {
		init();
		files = File.listRoots();
		run();
	}
	
	public FileWalker(String path) {
		init();
		File f = new File(path);
		files = f.listFiles();
		run();
	}
	
	public FileSnapshot getRoot(){
		return fileTree;
	}
	
	public FileSnapshot getRootRefersh(){
		run();
		return fileTree;
	}
	
	private void init(){
		fileTree.setFullName("Root");
		fileTree.setName("Root");
		fileTree.setFolder(true);
	}
	
	private void run(){
		List<FileSnapshot> fileList = new ArrayList<FileSnapshot>();
		try {
			for(File f : files)
			{
				FileSnapshot fileSnap = new FileSnapshot(f);
				fileList.add(fileSnap);
				CallableSearch call = new CallableSearch(f);
				fileSnap.setChildren(call.call());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		fileTree.setChildren(fileList);
	}
	
	private class CallableSearch implements Callable<List<FileSnapshot>>
	{
		File searchFolder;
		public CallableSearch(File file)
		{
			searchFolder = file;
		}
		@Override
		public List<FileSnapshot> call() throws Exception {
			return search(searchFolder);
		}
		List<FileSnapshot> search(File file)
		{
			if(file.isFile())
			{
				return null;
			}
			File[] listFiles = file.listFiles();
			if(listFiles == null)
			{
				return null;
			}
			Arrays.sort(listFiles, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					boolean isFolder1 = o1.isDirectory();
					boolean isFolder2 = o2.isDirectory();
					if(isFolder1 == isFolder2)
					{
						return  o1.getName().compareTo(o2.getName());
					}
					else
					{
						if(isFolder1)
						{
							return -1;
						}
						if(isFolder2)
						{
							return 1;
						}
					}
					return 0;
				}
			});
			List<FileSnapshot> files = new ArrayList<FileSnapshot>();
			for(File f : listFiles)
			{
				FileSnapshot e = new FileSnapshot(f);
				files.add(e);
				if(f.isDirectory())
				{
					e.setChildren(search(f));
				}
			}
			return files;
		}
		
	}
}
