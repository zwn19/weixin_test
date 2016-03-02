package com.zwn.entity.menu;


public class ClickButton extends BasicButton {
	public ClickButton() {
		super();
		setType("click");
	}

	private String key;
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
