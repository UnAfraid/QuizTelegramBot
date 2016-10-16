/*
 * Copyright (C) 2004-2015 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.unafraid.telegram.quizbot;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.unafraid.telegram.quizbot.database.DatabaseFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * @author UnAfraid
 */
public final class BotConfig
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BotConfig.class);
	public static String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
	public static int DATABASE_MAX_CONNECTIONS = 2;
	public static int DATABASE_MAX_IDLE_TIME = 0;
	public static long CONNECTION_CLOSE_TIME = 60 * 1000;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	
	private BotConfig()
	{
	}
	
	public static void load()
	{
		String dbUrl = System.getenv("JAWSDB_URL");
		if (dbUrl == null)
		{
			dbUrl = System.getenv("JAWSDB_MARIA_URL");
		}
		if (dbUrl != null)
		{
			try
			{
				final URI jdbUri = new URI(dbUrl);
				
				String username = jdbUri.getUserInfo().split(":")[0];
				String password = jdbUri.getUserInfo().split(":")[1];
				String port = String.valueOf(jdbUri.getPort());
				String jdbUrl = "jdbc:mysql://" + jdbUri.getHost() + ":" + port + jdbUri.getPath();
				
				DATABASE_URL = jdbUrl;
				DATABASE_LOGIN = username;
				DATABASE_PASSWORD = password;
				try (Connection con = DatabaseFactory.getInstance().getConnection();
					PreparedStatement ps = con.prepareStatement("SHOW TABLES LIKE ?");
					Statement st = con.createStatement())
				{
					ps.setString(1, "users");
					try (ResultSet rs = ps.executeQuery())
					{
						if (!rs.next())
						{
							st.execute(new String(Files.readAllBytes(Paths.get("dist/sql/users.sql"))));
						}
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.warn("Failed to setup database", e);
			}
			return;
		}
		final Config config = ConfigFactory.parseFile(new File("config/bot.conf"));
		
		// Database
		DATABASE_DRIVER = config.getString("database.driver");
		DATABASE_MAX_CONNECTIONS = config.getInt("database.max_connections");
		DATABASE_MAX_IDLE_TIME = config.getInt("database.max_idle_time");
		CONNECTION_CLOSE_TIME = config.getInt("database.connection_close_timer") * 1000;
		DATABASE_URL = config.getString("database.url");
		DATABASE_LOGIN = config.getString("database.login");
		DATABASE_PASSWORD = config.getString("database.password");
	}
}
