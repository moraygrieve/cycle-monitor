package com.jtech.iaf;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import it.sauronsoftware.cron4j.Scheduler;

import com.apama.util.Logger;
import com.apama.util.TimestampSet;
import com.apama.iaf.plugin.AbstractEventTransport;
import com.apama.iaf.plugin.EventDecoder;
import com.apama.iaf.plugin.EventTransport;
import com.apama.iaf.plugin.EventTransportProperty;
import com.apama.iaf.plugin.NormalisedEvent;
import com.apama.iaf.plugin.TimestampConfig;
import com.apama.iaf.plugin.TransportException;
import com.apama.iaf.plugin.TransportStatus;

public class CityBikesTransport extends AbstractEventTransport {
	private static final String PROPERTY_CITY_NAME = "cityName";
	private static final String PROPERTY_DATA_URL = "dataURL";
	private static final String PROPERTY_POLLING_SCHEDULE = "pollingSchedule";

	private Logger logger;
	private EventDecoder decoder;
	private String cityName;
	private String dataURL;
	private String pollingSchedule = null;
	private volatile boolean started;
	private volatile long totalSent;
	private volatile long totalReceived;
	private Scheduler scheduler;
	private ExecutorService exService;;

	public CityBikesTransport(String name, EventTransportProperty[] properties,
			TimestampConfig timestampConfig) throws TransportException {
		super(name, properties, timestampConfig);
		logger = Logger.getLogger(CityBikesTransport.class);
		updateProperties(properties, timestampConfig);
	}

	public synchronized void updateProperties(
			EventTransportProperty[] properties, TimestampConfig timestampConfig)
			throws TransportException {
		super.updateProperties(properties, timestampConfig);

		for (EventTransportProperty property : properties) {
			String name = property.getName();
			String value = property.getValue();

			if (PROPERTY_CITY_NAME.equals(name)) {
				cityName = value != null ? value : "";
				logger.info("Set city name to " + cityName);
			} else if (PROPERTY_DATA_URL.equals(name)) {
				dataURL = value;
				logger.info("Set data URL to " + dataURL);
			} else if (PROPERTY_POLLING_SCHEDULE.equals(name)) {
				pollingSchedule = value;
				logger.info("Set polling schedule to " + pollingSchedule);
			} else {
				continue;
			}
		}
	}

	@Override
	public int getAPIVersion() {
		return EventTransport.API_VERSION;
	}

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
	public synchronized void removeEventDecoder(String name)
			throws TransportException {
		decoder = null;
	}

	@Override
	public void sendTransportEvent(Object event, TimestampSet timestampSet)
			throws TransportException {
		totalReceived++;

		if (!(event instanceof NormalisedEvent)) {
			throw new TransportException("Invalid event type "
					+ event.getClass(), TransportException.DECODINGFAILURE);
		}

		NormalisedEvent normalisedEvent = (NormalisedEvent) event;
		String type = normalisedEvent.findValue("__type");
		if (type != null && type.equals("Start")) {
			if (started) return;
			logger.info("Received request to start");
			started = true;
			exService = Executors.newSingleThreadExecutor();
			scheduler = new Scheduler();
			
			if (pollingSchedule != null) {
				poll();
				scheduler.schedule(pollingSchedule, new Runnable() {
					public void run() { poll(); }
				});
				scheduler.start();
			}
		} else if (type != null && type.equals("Stop")) {
			if (!started) return;
			logger.info("Received request to stop");
			started = false;
			exService.shutdownNow();
			scheduler.stop();
		} else if (type != null && type.equals("Poll")) {
			if (!started) return;
			logger.info("Received request to poll for data");
			poll();
		}
	}

	@Override
	public void start() throws TransportException {
	}

	@Override
	public void stop() throws TransportException {
		started = false;
	}

	@Override
	public void cleanup() throws TransportException {
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
			BufferedReader br = new BufferedReader(new InputStreamReader(
					urlc.getInputStream()));
			JSONParser parser = new JSONParser();
			return (JSONArray) parser.parse(br);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	private void processJSON(JSONArray jsonArray) {
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
				while (regexMatcher.find()) {
					name = regexMatcher.group(2);
				}

				Double lat = Double.valueOf((Long) entry.get("lat")) / 1000000d;
				Double lng = Double.valueOf((Long) entry.get("lng")) / 1000000d;
				Long avail = (Long) entry.get("bikes");
				Long empty = (Long) entry.get("free");

				NormalisedEvent normalisedEvent = new NormalisedEvent();
				normalisedEvent.addQuick("__type", "__station_update");
				normalisedEvent.addQuick("city", cityName);
				normalisedEvent.addQuick("id", id.toString());
				normalisedEvent.addQuick("name", name);
				normalisedEvent.addQuick("lat", lat.toString());
				normalisedEvent.addQuick("lng", lng.toString());
				normalisedEvent.addQuick("updated", time);
				normalisedEvent.addQuick(
						"ratio",
						String.format(
								"%.2f",
								Double.valueOf(avail)
										/ Double.valueOf((avail + empty))));
				normalisedEvent.addQuick("docked", avail.toString());
				normalisedEvent.addQuick("empty", empty.toString());
				normalisedEvent.addQuick("timestamp",
						String.valueOf(System.currentTimeMillis() / 1000));

				logger.info("Sending downstream transport event to data codec: "
						+ normalisedEvent);

				TimestampSet timestampSet = new TimestampSet();
				decoder.sendTransportEvent(normalisedEvent, timestampSet);
				totalReceived++;
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
