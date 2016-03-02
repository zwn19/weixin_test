package com.zwn.entity.menu;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

public class WrapButton {
	private List<BasicButton> button;

	public void addButton(BasicButton b){
		if(button == null){
			button = new ArrayList<BasicButton>();
		}
		button.add(b);
	}
	
	public List<BasicButton> getButton() {
		return button;
	}
	
	public String toJsonString(){
		JSONObject json = JSONObject.fromObject(this);
		return json.toString().replace("\"sub_button\":[]", "").replace(",,",",");
	}
}
