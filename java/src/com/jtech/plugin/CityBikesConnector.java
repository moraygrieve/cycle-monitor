package com.jtech.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.softwareag.connectivity.AbstractTransport;
import com.softwareag.connectivity.MapHelper;
import com.softwareag.connectivity.Message;

public class CityBikesConnector extends AbstractTransport {
	private final String cityName;
	private final String dataURL;
	private volatile boolean startRequested;
	private ExecutorService exService;;
	
	public CityBikesConnector(Map<String, Object> config, String chainId, Logger logger)
			throws Exception, IllegalArgumentException {
		super(config, chainId, logger);
		cityName = (String) MapHelper.getString(config, "city");
		dataURL = (String) MapHelper.getString(config, "url");
	}

	@Override
	public void sendBatchTowardsTransport(List<Message> messages) {
		LOGGER.info("Received upstream event from the host ..." + messages.size());
		
		for (Message msg : messages) {
			String type = msg.getMetadata().get("sag.type");
			
			if (type != null && type.equals("com.jtech.source.Start")) {
				if (startRequested) return;
				LOGGER.info("Received request to start");
				startRequested = true;
				exService = Executors.newSingleThreadExecutor();
				poll();
			} else if (type != null && type.equals("com.jtech.source.Stop")) {
				if (!startRequested) return;
				LOGGER.info("Received request to stop");
				startRequested = false;
				exService.shutdownNow();
			} else if (type != null && type.equals("com.jtech.source.Poll")) {
				if (!startRequested) return;
				LOGGER.info("Received request to poll for data");
				poll();
			}
		}
	}

	@Override
	public void hostReady() throws IOException {
		LOGGER.info("Received host ready notification from the correlator");
	}

	@Override
	public void shutdown() throws Exception {
		LOGGER.info("Shutdown requested, stopping scheduler");
		exService.shutdownNow();
		super.shutdown();
	}

	private synchronized void poll() {
		exService.execute(new Runnable() {
			public void run() {
				JSONArray jsonArray = getJSON();
				processJSON(jsonArray);
			}
		});
	}

	private JSONArray getJSON() {
		try {
			URL page = new URL(dataURL);
			URLConnection urlc = (URLConnection) page.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
			JSONParser parser = new JSONParser();
			return (JSONArray) parser.parse(br);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}

	private void processJSON(JSONArray jsonArray) {
		try {
			for (Object o : jsonArray) {
				JSONObject entry = (JSONObject) o;
				Long id = (Long) entry.get("id");
				String name = (String) entry.get("name");

				Pattern regex = Pattern.compile("^(\\d+)\\s?-\\s?(.*)");
				Matcher regexMatcher = regex.matcher(name);
				while (regexMatcher.find()) { name = regexMatcher.group(2); }

				Double lat = Double.valueOf((Long) entry.get("lat")) / 1000000d;
				Double lng = Double.valueOf((Long) entry.get("lng")) / 1000000d;
				Long avail = (Long) entry.get("bikes");
				Long empty = (Long) entry.get("free");
				String updated = (String) entry.get("timestamp");

				Map<String, Object> payload = new HashMap<>();
				
				payload.put("__type", "__station_update");
				payload.put("city", cityName);
				payload.put("id", id.toString());
				payload.put("name", name);
				payload.put("lat", lat.toString());
				payload.put("lng", lng.toString());
				payload.put("updated", updated);
				payload.put("ratio",String.format("%.2f",Double.valueOf(avail)/ Double.valueOf((avail + empty))));
				payload.put("docked", avail.toString());
				payload.put("empty", empty.toString());
				payload.put("timestamp",String.valueOf(System.currentTimeMillis() / 1000));

				LOGGER.info("Sending downstream transport event to data codec: "+ payload);

				Map<String, String> metadata = new HashMap<String, String>();
				metadata.put(Message.HOST_MESSAGE_TYPE, "com.jtech.source.StationUpdate");
				Message msg = new Message(payload, metadata);
				hostSide.sendBatchTowardsHost(Collections.singletonList(msg));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}
}
