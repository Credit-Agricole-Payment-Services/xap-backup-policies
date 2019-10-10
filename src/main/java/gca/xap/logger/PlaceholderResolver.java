package gca.xap.logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class PlaceholderResolver {

	public String getHost() {
		String hostName;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException("Failed to get the host name", e);
		}
		int fistDotIndex = hostName.indexOf(".");
		if (fistDotIndex > 0) {
			return hostName.substring(0, fistDotIndex);
		}
		return hostName;
	}

	public String getService() {
		String serviceValue = System.getProperty("gs.logFileName");
		serviceValue = serviceValue.substring(0, serviceValue.lastIndexOf("_"));
		serviceValue = serviceValue + "_[0-9]*";
		return serviceValue;
	}

}
