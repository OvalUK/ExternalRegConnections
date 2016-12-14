package com.uk.oval.registrations;

import java.io.Serializable;

import lotus.domino.Document;
import lotus.domino.NotesException;

import com.ibm.commons.util.StringUtil;
import com.ibm.connections.directory.services.util.ObjectGUIDConverter;

public class Person implements Serializable {

	private static final long serialVersionUID = -3200582426739293345L;

	private String firstName;
	private String lastName;
	private String org;
	private String tel;
	private String email;
	private String password;
	private String passwordConfirm;
	private Boolean hasDummyPassword;
	private String communityName;
	private String communityGUID;
	private String docID;
	private String shortName;

	public Person() {
		//default
		org = "External";
		hasDummyPassword = true;
	}
	
	public void loadFromDoc(Document doc) throws NotesException {
		//validate state
		if (null == doc) throw new IllegalArgumentException("Person doc is null");
		
		firstName = doc.getItemValueString("FirstName");
		lastName = doc.getItemValueString("LastName");
		org = doc.getItemValueString("o");
		email = doc.getItemValueString("InternetAddress");
		shortName = doc.getItemValueString("ShortName");
		docID = doc.getUniversalID();
	}
	
	public Boolean getHasDummyPassword() {
		return hasDummyPassword;
	}

	public void setHasDummyPassword(Boolean hasDummyPassword) {
		this.hasDummyPassword = hasDummyPassword;
	}

	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFullName() {
		if (StringUtil.isEmpty(org)) {
			return "CN=" + getCommonName();
		} else {
			return "CN=" + getCommonName() + "/O=" + org;
		}
	}

	public String getCommonName() {
		return firstName + " " + lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getCommunityName() {
		return communityName;
	}

	public void setCommunityName(String communityName) {
		this.communityName = communityName;
	}

	public String getCommunityGUID() {
		return communityGUID;
	}

	public void setCommunityGUID(String communityGUID) {
		this.communityGUID = communityGUID;
	}

	public String getProfileGUID() {
		// calculate guid from doc id using same method as connections TDI
		// assembly
		/*
		 * var byteString = null;
    var canonicalString = "";
    var attr = work.getAttribute("dominoUNID");

    if(attr != null) {
        byteString = attr.getValue(0);

        if(byteString != null) {
            canonicalString = com.ibm.connections.directory.services.util.ObjectGUIDConverter.convertByteStringToGUIDString(byteString);
        } 
    }
    return canonicalString;

		 */
		String guid = "";
		guid = ObjectGUIDConverter.convertByteStringToGUIDString(docID);
		return guid;
	}

	public String getDocID() {
		return docID;
	}

	public void setDocID(String docID) {
		this.docID = docID;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
}
