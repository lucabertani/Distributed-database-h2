package it.lucabertani.communication.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PackagePing extends Package {

	public PackagePing(@JsonProperty("serverId") String serverId) {
		super(serverId, PackageType.MULTICAST_PING);
	}

}
