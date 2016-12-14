package com.uk.oval.connections;

import java.io.Serializable;

import com.ibm.sbt.services.client.ClientServicesException;
import com.ibm.sbt.services.client.base.datahandlers.EntityList;
import com.ibm.sbt.services.client.connections.activitystreams.ActivityStreamEntityList;
import com.ibm.sbt.services.client.connections.activitystreams.ActivityStreamService;
import com.ibm.sbt.services.client.connections.communities.Community;
import com.ibm.sbt.services.client.connections.communities.CommunityService;
import com.ibm.sbt.services.client.connections.files.File;
import com.ibm.sbt.services.client.connections.files.FileService;
import com.ibm.sbt.services.client.connections.profiles.Profile;
import com.ibm.sbt.services.client.connections.profiles.ProfileService;
import com.ibm.sbt.services.endpoints.ConnectionsBasicEndpoint;
import com.ibm.sbt.services.endpoints.EndpointFactory;

public class ConnectionsServiceBase implements Serializable {
	private static final long serialVersionUID = 1L;
	private String endpointName = "connections";

	public String getEndpointName() {
		return endpointName;
	}

	public void setEndpointName(String endpointName) {
		this.endpointName = endpointName;
	}

	public Boolean getIsAuthenticated() {
		try {
			return getBasicEndpoint().isAuthenticated();
		} catch (ClientServicesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public ConnectionsBasicEndpoint getBasicEndpoint() {
		return (ConnectionsBasicEndpoint) EndpointFactory
				.getEndpoint(endpointName);
	}

	public ActivityStreamEntityList getAllStatusUpdates() {
		ActivityStreamService service = new ActivityStreamService();
		try {
			return service.getAllUpdates();
		} catch (Throwable e) {
			return null;
		}
	}

	public ActivityStreamEntityList getMyStatusUpdates() {
		ActivityStreamService service = new ActivityStreamService();
		try {
			return service.getMyStatusUpdates();
		} catch (Throwable e) {
			return null;
		}
	}

	public ActivityStreamEntityList getMyNetworkStatusUpdates() {
		ActivityStreamService service = new ActivityStreamService();
		try {
			return service.getStatusUpdatesFromMyNetwork();
		} catch (Throwable e) {
			return null;
		}
	}

	public ActivityStreamEntityList getMyNetworkUpdates() {
		ActivityStreamService service = new ActivityStreamService();
		try {
			return service.getUpdatesFromMyNetwork();
		} catch (Throwable e) {
			return null;
		}
	}

	public ActivityStreamEntityList getUpdatesIFollow() {
		ActivityStreamService service = new ActivityStreamService();
		try {
			return service.getUpdatesFromPeopleIFollow();
		} catch (Throwable e) {
			return null;
		}
	}

	public EntityList<File> getMyFiles() {
		FileService service = new FileService();
		try {
			return service.getMyFiles();
		} catch (Exception e) {
			return null;
		}

	}

	public EntityList<Profile> getMyColleagues() {
		ProfileService service = new ProfileService();
		try {
			Profile profile = service.getMyProfile();
			EntityList<Profile> profiles = service.getColleagues(profile
					.getUserid());
			return profiles;
		} catch (Throwable e) {
			return null;
		}
	}

	public Profile getMyProfile() {
		ProfileService service = new ProfileService();
		try {
			Profile profile = service.getMyProfile();
			return profile;
		} catch (Throwable e) {
			return null;
		}
	}




	public EntityList<Community> getAllCommunities() {
		CommunityService svc = new CommunityService();
		try {
			EntityList<Community> comms = svc.getPublicCommunities();
			return comms;
		} catch (Throwable e) {
			return null;
		}
	}

	public EntityList<Community> getMyCommunities() {
		CommunityService svc = new CommunityService();
		try {
			EntityList<Community> comms = svc.getMyCommunities();
			return comms;
		} catch (Throwable e) {
			return null;
		}
	}

	

}