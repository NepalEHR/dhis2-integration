package com.possible.dhis2int.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.possible.dhis2int.Properties;
import com.possible.dhis2int.web.DHISIntegratorException;
import com.possible.dhis2int.web.Messages;

@Service
public class DatabaseDriver {

	private Properties properties;

	@Autowired
	public DatabaseDriver(Properties properties) {
		this.properties = properties;
	}

	public Results executeQuery(String formattedSql, String type) throws DHISIntegratorException {
		Connection connection = null;
		try {

			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			if ("ElisGeneric".equalsIgnoreCase(type)) {
				connection = DriverManager.getConnection(properties.openelisDBUrl);
			}
			ResultSet resultSet = connection.createStatement().executeQuery(formattedSql);
			return Results.create(resultSet);
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format(Messages.SQL_EXECUTION_EXCEPTION, formattedSql), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

	public void createTempTable(Integer numberOfMaleLessThanSix, Integer numberOfFemalesLessThanSix,
			Integer numberOfMalesMoreThanSix, Integer numberOfFemalesMoreThanSix) throws DHISIntegratorException {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(properties.openmrsDBUrl);
			Statement statement = connection.createStatement();
			statement.executeUpdate("DROP TEMPORARY TABLE IF EXISTS imam");
			statement.executeUpdate(
					"CREATE TEMPORARY TABLE imam(male_less_than_six int, female_less_than_six int, male_more_than_six int, female_more_than_six int)");
			String insertImamData = new StringBuffer(
					"INSERT INTO imam(male_less_than_six , female_less_than_six , male_more_than_six , female_more_than_six) SELECT ")
							.append(numberOfMaleLessThanSix).append(", ").append(numberOfFemalesLessThanSix)
							.append(", ").append(numberOfMalesMoreThanSix).append(", ")
							.append(numberOfFemalesMoreThanSix).toString();
			statement.executeUpdate(insertImamData);
		} catch (SQLException e) {
			throw new DHISIntegratorException(String.format("Failed to create temporary table imam"), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
				}
			}
		}
	}

}