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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * @author UnAfraid
 */
public final class BotConfig
{	
	public static String DATABASE_DRIVER;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_MAX_IDLE_TIME;
	public static long CONNECTION_CLOSE_TIME;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	
	private BotConfig()
	{
	}
	
	public static void load()
	{
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
