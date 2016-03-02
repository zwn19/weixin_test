package com.zwn.entity.menu;

import java.util.ArrayList;
import java.util.List;

public class BasicButton {
	private String type;
	
	private String name;
	
	private List<BasicButton> sub_button;
	
	public void addButton(BasicButton b){
		if(sub_button == null){
			sub_button = new ArrayList<BasicButton>();
		}
		sub_button.add(b);
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<BasicButton> getSub_button() {
		return sub_button;
	}

	public static void main(String[] args) {
		
	}
}
