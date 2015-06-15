package com.jtech.iaf;

import it.sauronsoftware.cron4j.Scheduler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.apama.util.Logger;
import com.apama.iaf.plugin.AbstractEventTransport;
import com.apama.iaf.plugin.EventDecoder;
import com.apama.iaf.plugin.EventTransport;
import com.apama.iaf.plugin.EventTransportProperty;
import com.apama.iaf.plugin.TimestampConfig;
import com.apama.iaf.plugin.TransportException;
import com.apama.iaf.plugin.TransportStatus;
import com.apama.util.TimestampSet;

public class CityBikesTransport extends AbstractEventTransport {
	private static final String PROPERTY_CITY_NAME = "cityName";
	private static final String PROPERTY_DATA_URL = "dataURL";
	private static final String PROPERTY_POLLING_INTERVAL = "pollingInterval";

	/** Private data members */
	private Logger logger;
	private String transportName;
	private EventDecoder decoder;
	private String cityName;
	private String dataURL;
	private long pollingInterval;

	/** Used for status reporting */
	private volatile boolean started;
	private volatile long totalSent;
	private volatile long totalReceived;


	public CityBikesTransport(String name, EventTransportProperty[] properties,
			TimestampConfig timestampConfig) throws TransportException {
		super(name, properties, timestampConfig);
		transportName = name;
		logger = Logger.getLogger(CityBikesTransport.class);
		updateProperties(properties, timestampConfig);
		logger.info("Created the city bikes transport");
	}

	public synchronized void updateProperties(EventTransportProperty[] properties,
			TimestampConfig timestampConfig) 
					throws TransportException
	{
		super.updateProperties(properties, timestampConfig);

		for (EventTransportProperty property : properties)
		{
			String name = property.getName();
			String value = property.getValue();

			if (PROPERTY_CITY_NAME.equals(name)) {
				cityName = value!=null?value:"";
				logger.info("Set city name to " + cityName);
			} 
			else if (PROPERTY_DATA_URL.equals(name)) {
				dataURL = value;
				logger.info("Set data URL to " + dataURL);
			}
			else if (PROPERTY_POLLING_INTERVAL.equals(name)) {
				try {
					pollingInterval = Long.parseLong(value);
					logger.info("Set polling interval to " + pollingInterval);
				}
				catch(NumberFormatException e) { };
			}
			else {
				continue;
			}
		}
	}

	@Override
	public int getAPIVersion() { return EventTransport.API_VERSION; }

	@Override
	public TransportStatus getStatus() {
		String status = (started) ? "Plugin is started" : "Plugin is stopped";
		return new TransportStatus(status, totalReceived, totalSent);
	}

	@Override
	public synchronized void addEventDecoder(String name, EventDecoder decoder)
			throws TransportException {
		this.decoder = decoder;
	}

	@Override
	public synchronized void removeEventDecoder(String name) throws TransportException {
		decoder = null;
	}

	@Override
	public void sendTransportEvent(Object event, TimestampSet timestampSet)
			throws TransportException {
	}

	@Override
	public void start() throws TransportException {
		synchronized(this) {
			started = true;
			
			Scheduler s = new Scheduler();
			s.schedule("* * * * *", new Runnable() {
				public void run() { poll(); }
			});
			s.start();
		}
	}

	@Override
	public void stop() throws TransportException {
		synchronized(this) {
			started = false;
		}	
	}

	@Override
	public void cleanup() throws TransportException {}

	private void poll() {
		JSONArray jsonArray = getJSON();
		load(jsonArray);
	}
	
	private JSONArray getJSON() {
		try {
			URL page = new URL(dataURL);
			URLConnection urlc = (URLConnection) page.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
			JSONParser parser = new JSONParser();
			return (JSONArray) parser.parse(br);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void load(JSONArray jsonArray) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			String time = dateFormat.format(cal.getTime());
			
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
				
				logger.info(name + ", bikes="+avail+", empty="+empty);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
