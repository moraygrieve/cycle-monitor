package com.jtech.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Connect {
	private Connection connect = null;
	private Statement statement = null;
	private ResultSet resultSet = null;

	public void readDataBase() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/cycle_monitor?"
					+ "user=root&password=root");

			statement = connect.createStatement();
			resultSet = statement.executeQuery("select * from boundary_alert_config");
			writeResultSet(resultSet);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void writeResultSet(ResultSet resultSet) throws SQLException {
		while (resultSet.next()) {
			String id = resultSet.getString("id");
			String city = resultSet.getString("city");
			String upper = resultSet.getString("upper");
			String threshold = resultSet.getString("threshold");
			System.out.println("id: " + id);
			System.out.println("city: " + city);
			System.out.println("upper: " + upper);
			System.out.println("threshold: " + threshold);
		}
	}

	public static void main(String[] args) {
		Connect connect = new Connect();
		connect.readDataBase();
	}
}
