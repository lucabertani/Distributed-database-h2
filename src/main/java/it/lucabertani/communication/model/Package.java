package it.lucabertani.communication.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Package {

	private final String serverId;
	private final PackageType type;

	public Package(String serverId, PackageType type) {
		super();
		this.serverId = serverId;
		this.type = type;
	}

	public String getServerId() {
		return serverId;
	}

	public PackageType getType() {
		return type;
	}
	
	public String toJson() {
		ObjectMapper objectMapper = getObjectMapper();
		
		try {
			return objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	
	
	private static final ThreadLocal<ObjectMapper> om = new ThreadLocal<ObjectMapper>() {
	    @Override
	    protected ObjectMapper initialValue() {
	        ObjectMapper objectMapper = new ObjectMapper();
	        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	        return objectMapper;
	    }
	};

	public static ObjectMapper getObjectMapper() {
	    return om.get();
	}
}
