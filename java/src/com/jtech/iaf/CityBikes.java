package com.jtech.iaf;

import com.apama.iaf.plugin.AbstractEventTransport;
import com.apama.iaf.plugin.EventDecoder;
import com.apama.iaf.plugin.EventTransportProperty;
import com.apama.iaf.plugin.TimestampConfig;
import com.apama.iaf.plugin.TransportException;
import com.apama.iaf.plugin.TransportStatus;
import com.apama.util.TimestampSet;


public class CityBikes extends AbstractEventTransport {

	public CityBikes(String name, EventTransportProperty[] properties,
			TimestampConfig timestampConfig) throws TransportException {
		super(name, properties, timestampConfig);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addEventDecoder(String arg0, EventDecoder arg1)
			throws TransportException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cleanup() throws TransportException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getAPIVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TransportStatus getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeEventDecoder(String arg0) throws TransportException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendTransportEvent(Object arg0, TimestampSet arg1)
			throws TransportException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() throws TransportException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws TransportException {
		// TODO Auto-generated method stub
		
	}
}