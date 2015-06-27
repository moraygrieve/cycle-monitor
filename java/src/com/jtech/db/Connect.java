// Cycle Monitor, Copyright (C) 2015  M.B.Grieve
// This file is part of the Cycle Monitor example application.
//
// Cycle Monitor is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Cycle Monitor is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with City Monitor.  If not, see <http://www.gnu.org/licenses/>.
//
// Contact: moray.grieve@me.com

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
