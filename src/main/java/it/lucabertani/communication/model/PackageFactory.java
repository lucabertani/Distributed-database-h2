package it.lucabertani.communication.model;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PackageFactory {

	public static Optional<Package> convertToPackage(String json) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			JsonNode node = objectMapper.readTree(json);
			String type = node.get("type").asText();
			
			PackageType packageType = PackageType.valueOf(type);
			
			Package p = switch(packageType) {
			case MULTICAST_PING: {
				Package pack = objectMapper.readValue(json, PackagePing.class);
				yield pack;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + packageType);
			};
			
			return Optional.of(p);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return Optional.empty();
	}
}
