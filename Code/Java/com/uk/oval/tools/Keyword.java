package com.uk.oval.tools;

import java.io.Serializable;
import java.util.HashMap;

public class Keyword extends HashMap<String, Object> implements Serializable {

	private static final long serialVersionUID = -46214382493518220L;

	String key = "";
	

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Keyword(String key) {
		this.key = key;
	}
	
	public String toString() {
		//for dbs print out the full path in datasource friendly format
		//i.e. servername!!path
		if (key.startsWith("db")) {
			return this.get("server") + "!!" + this.get("path");
		} else {
			return (String)this.get("value1");
		}
	}
	
}
