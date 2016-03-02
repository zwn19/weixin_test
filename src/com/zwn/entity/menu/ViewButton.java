package com.zwn.entity.menu;


public class ViewButton extends BasicButton {
	private String url;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ViewButton() {
		super();
		setType("view");
	}

}
