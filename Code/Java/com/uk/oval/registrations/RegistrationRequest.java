package com.uk.oval.registrations;


import lotus.domino.Document;
import lotus.domino.NotesException;

public class RegistrationRequest extends Request {

	private static final long serialVersionUID = 7585723614024408784L;

	private String communityName;
	private String communityGUID;
	
	public RegistrationRequest() {
		type = Request.REQUESTTYPE_REGISTRATION;
	}
	public RegistrationRequest(Document doc) throws RegistrationException {
		super(doc);
	}
	
	@Override
	protected void saveAdditional(Document doc) throws NotesException {
		doc.replaceItemValue("DefaultCommunity", communityName);
		doc.replaceItemValue("DefaultCommunityGUID", communityGUID);
	}
	@Override
	protected void loadFromDocAdditional(Document doc) throws NotesException {
		communityName = doc.getItemValueString("DefaultCommunity");
		communityGUID = doc.getItemValueString("DefaultCommunityGUID");
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
}
