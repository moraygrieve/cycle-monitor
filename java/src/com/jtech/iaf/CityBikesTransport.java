package com.jtech.iaf;

import com.apama.iaf.plugin.AbstractEventTransport;
import com.apama.iaf.plugin.EventDecoder;
import com.apama.iaf.plugin.EventTransport;
import com.apama.iaf.plugin.EventTransportProperty;
import com.apama.util.Logger;
import com.apama.iaf.plugin.TimestampConfig;
import com.apama.iaf.plugin.TransportException;
import com.apama.iaf.plugin.TransportStatus;
import com.apama.util.TimestampSet;


public class CityBikesTransport extends AbstractEventTransport {
	private static final String PROPERTY_CITY_NAME = "cityName";
	private static final String PROPERTY_DATA_URL = "dataURL";
	private static final String PROPERTY_POLLING_INTERVAL = "pollingInterval";

	private String transportName;
	private EventDecoder decoder;
	private Logger logger;
	private boolean started;
	private long totalSent;
	private long totalReceived;
	private String cityName;
	private String dataURL;
	private long pollingInterval;

	public CityBikesTransport(String name, EventTransportProperty[] properties,
			TimestampConfig timestampConfig) throws TransportException {
		super(name, properties, timestampConfig);
		updateProperties(properties, timestampConfig);
		transportName = name;
		logger = Logger.getLogger(CityBikesTransport.class);
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
				cityName = value;
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
	public void addEventDecoder(String name, EventDecoder decoder)
			throws TransportException {
		this.decoder = decoder;
	}

	@Override
	public void cleanup() throws TransportException {}

	@Override
	public int getAPIVersion() { return EventTransport.API_VERSION; }

	@Override
	public TransportStatus getStatus() {
		String status = (started) ? "Plugin is started" : "Plugin is stopped";
		return new TransportStatus(status, totalReceived, totalSent);
	}

	@Override
	public void removeEventDecoder(String name) throws TransportException {
		decoder = null;
	}

	@Override
	public void sendTransportEvent(Object event, TimestampSet timestampSet)
			throws TransportException {
	}

	@Override
	public void start() throws TransportException {
		started = true;
	}

	@Override
	public void stop() throws TransportException {
		started = false;
	}
}