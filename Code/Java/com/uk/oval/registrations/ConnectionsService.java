package com.uk.oval.registrations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.commons.util.StringUtil;
import com.ibm.sbt.services.client.ClientServicesException;
import com.ibm.sbt.services.client.base.datahandlers.EntityList;
import com.ibm.sbt.services.client.connections.communities.Community;
import com.ibm.sbt.services.client.connections.communities.CommunityService;
import com.ibm.sbt.services.client.connections.communities.Invite;
import com.ibm.sbt.services.client.connections.communities.Member;
import com.ibm.sbt.services.client.connections.profiles.Profile;
import com.ibm.sbt.services.client.connections.profiles.ProfileAdminService;
import com.ibm.sbt.services.client.connections.profiles.ProfileService;
import com.ibm.sbt.services.client.connections.profiles.utils.ProfilesConstants;
import com.ibm.sbt.services.endpoints.ConnectionsBasicEndpoint;
import com.uk.oval.connections.ConnectionsServiceBase;
import com.uk.oval.tools.Keyword;
import com.uk.oval.tools.Settings;

import eu.linqed.debugtoolbar.DebugToolbar;

public class ConnectionsService extends ConnectionsServiceBase {

	private static final long serialVersionUID = 1L;

	private static final DebugToolbar log = DebugToolbar.get();

	public ConnectionsService() {
		loginAsAdmin();
	}

	public void createProfile(Person person) throws ClientServicesException {
		//validate
		if (StringUtil.isEmpty(person.getProfileGUID())) throw new IllegalArgumentException("GUID is blank");
		if (StringUtil.isEmpty(person.getFullName())) throw new IllegalArgumentException("Full name is blank");
		if (StringUtil.isEmpty(person.getLastName())) throw new IllegalArgumentException("Last name is blank");
		if (StringUtil.isEmpty(person.getShortName())) throw new IllegalArgumentException("Short name is blank");
		
		ProfileAdminService service = new ProfileAdminService(); 
		Profile profile = new Profile(service, person.getShortName());
		profile.setAsString("guid",	person.getProfileGUID());
		profile.setAsString("uid", person.getShortName());
		profile.setAsString("distinguishedName",person.getFullName());
		profile.setAsString("displayName", person.getCommonName());
		profile.setAsString("givenNames", person.getFirstName());
		profile.setAsString("surname", person.getLastName());
		profile.setAsString("email", person.getEmail());
		profile.setAsString(ProfilesConstants.ProfileAttribute.USER_MODE.getEntityName(),"external");
		
		service.createProfile(profile);
	}
	
	public void loginAsAdmin() {
		Settings settingsSecure = Settings.getBean("securesettings");
		Keyword connectionsUser = settingsSecure.get("connections_admin");
		String user = connectionsUser.get("value1").toString();
		String password = connectionsUser.get("value2").toString();

		try {
			ConnectionsBasicEndpoint endPoint = getBasicEndpoint();
			endPoint.login(user, password, true);
			if (endPoint.isAuthenticated()) {
				log.debug("logged in as " + user);
			} else {
				log.debug("able to login as " + user);
			}

		} catch (Exception e) {
			log.error(e);
		}
	}

	public void logout() {
		try {
			ConnectionsBasicEndpoint endPoint = getBasicEndpoint();
			endPoint.logout();
		} catch (Exception e) {
			log.error(e);
		}
	}

	public EntityList<Community> findCommunitiesByName(String name)
			throws ClientServicesException {
		CommunityService communityService = new CommunityService();
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", name);
		return communityService.getPublicCommunities(params);
	}

	public Community getCommunity(String guid) throws ClientServicesException {
		CommunityService communityService = new CommunityService();
		return communityService.getCommunity(guid);
	}

	public List<Profile> findMembersByName(String name) {
		ProfileService service = new ProfileService();
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", name);

		try {
			List<Profile> profilesReturn = new ArrayList<Profile>();
			EntityList<Profile> profiles = service.searchProfiles(params);
			profilesReturn.addAll(profiles);

			return profilesReturn;
		} catch (Exception e) {
			log.error(e);
			return null;
		}
	}

	public void addMemberToCommunity(String communityUuid, String memberID)
			throws ClientServicesException {
		addMemberToCommunity(communityUuid, memberID, false);
	}

	public void addMemberToCommunity(String communityUuid, String memberID,
			Boolean inviteUser) throws ClientServicesException {

		CommunityService communityService = new CommunityService();

		/*
		 * Community community = communityService.getCommunity(communityUuid);
		 * log.debug("Community=" + community.getTitle()); ProfileService
		 * service = new ProfileService(); Profile profile =
		 * service.getProfile(memberID); log.debug("Person=" +
		 * profile.getName()); String id = profile.getUserid(); log.debug("id="
		 * + id);
		 * 
		 * log.debug("Member name=" + newMember.getName());
		 */
		Member newMember = new Member(communityService, memberID);

		log.debug("user : " + memberID + " Community "
				+ communityUuid);
		
		if (inviteUser) {
			Invite invite = new Invite(communityService);
			invite.setCommunityUuid(communityUuid);
			invite.setId(memberID);
			
			invite.setUserid(memberID);
			
			if (StringUtil.isEmpty(newMember.getUserid())) {
				invite.setEmail(newMember.getEmail());
			} else {
				invite.setUserid(newMember.getUserid());
			}
			
			invite = communityService.createInvite(invite);

			log.debug("user : " + memberID + " invited to community "
					+ communityUuid);
		} else {
			communityService.addMember(communityUuid, newMember);
			log.debug("user : " + memberID + " added to community "
					+ communityUuid);
		}

	}

	public void addMember() {
		try {
			CommunityService communityService = new CommunityService();
			Map<String, String> paramsCom = new HashMap<String, String>();
			paramsCom.put("name", "External Test 2");
			EntityList<Community> communities = communityService
					.getMyCommunities(paramsCom);

			Community community = communities.iterator().next();

			community = communityService
					.getCommunity("8f081451-9af9-4084-a66b-3c5eb6b8a632");

			System.out.print("Community=" + community.getTitle());

			ProfileService service = new ProfileService();
			Map<String, String> params = new HashMap<String, String>();
			params.put("name", "Adam");
			EntityList<Profile> profiles = service.searchProfiles(params);
			Profile profile = profiles.get(0);
			System.out.print("Person=" + profile.getName());

			String id = profile.getUserid();
			System.out.print("id=" + id);
			Member newMember = new Member(communityService, id);

			System.out.print("Member name=" + newMember.getName());

			communityService.addMember(community.getCommunityUuid(), newMember);
			System.out.println("user : " + id + " added to community "
					+ community.getCommunityUuid());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static Boolean isExternal(Community community) {
		return StringUtil.equals("true", community
				.getAsString("./snx:isExternal"));
	}

}
