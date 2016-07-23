/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.unafraid.telegram.quizbot.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.unafraid.telegram.quizbot.BotConfig;
import com.github.unafraid.telegram.quizbot.util.ThreadPoolManager;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author UnAfraid
 */
public class DatabaseFactory
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseFactory.class);
	
	private static DatabaseFactory _instance;
	private ComboPooledDataSource _source;
	
	/**
	 * Instantiates a new database factory.
	 */
	public DatabaseFactory()
	{
		try
		{
			if (BotConfig.DATABASE_MAX_CONNECTIONS < 2)
			{
				BotConfig.DATABASE_MAX_CONNECTIONS = 2;
				LOGGER.warn("A minimum of {} db connections are required.", BotConfig.DATABASE_MAX_CONNECTIONS);
			}
			
			_source = new ComboPooledDataSource();
			_source.setAutoCommitOnClose(true);
			
			_source.setInitialPoolSize(10);
			_source.setMinPoolSize(10);
			_source.setMaxPoolSize(Math.max(10, BotConfig.DATABASE_MAX_CONNECTIONS));
			
			_source.setAcquireRetryAttempts(0); // try to obtain connections indefinitely (0 = never quit)
			_source.setAcquireRetryDelay(500); // 500 milliseconds wait before try to acquire connection again
			_source.setCheckoutTimeout(0); // 0 = wait indefinitely for new connection
			// if pool is exhausted
			_source.setAcquireIncrement(5); // if pool is exhausted, get 5 more connections at a time
			// cause there is a "long" delay on acquire connection
			// so taking more than one connection at once will make connection pooling
			// more effective.
			
			// this "connection_test_table" is automatically created if not already there
			_source.setAutomaticTestTable("connection_test_table");
			_source.setTestConnectionOnCheckin(false);
			
			// testing OnCheckin used with IdleConnectionTestPeriod is faster than testing on checkout
			
			_source.setIdleConnectionTestPeriod(3600); // test idle connection every 60 sec
			_source.setMaxIdleTime(BotConfig.DATABASE_MAX_IDLE_TIME); // 0 = idle connections never expire
			// *THANKS* to connection testing configured above
			// but I prefer to disconnect all connections not used
			// for more than 1 hour
			
			// enables statement caching, there is a "semi-bug" in c3p0 0.9.0 but in 0.9.0.2 and later it's fixed
			_source.setMaxStatementsPerConnection(100);
			
			_source.setBreakAfterAcquireFailure(false); // never fail if any way possible
			// setting this to true will make
			// c3p0 "crash" and refuse to work
			// till restart thus making acquire
			// errors "FATAL" ... we don't want that
			// it should be possible to recover
			_source.setDriverClass(BotConfig.DATABASE_DRIVER);
			_source.setJdbcUrl(BotConfig.DATABASE_URL);
			_source.setUser(BotConfig.DATABASE_LOGIN);
			_source.setPassword(BotConfig.DATABASE_PASSWORD);
			
			/* Test the connection */
			_source.getConnection().close();
		}
		catch (Exception e)
		{
			LOGGER.warn("Couldn't initialize database: ", e);
		}
	}
	
	/**
	 * Shutdown.
	 */
	public void shutdown()
	{
		try
		{
			_source.close();
		}
		catch (Exception e)
		{
			LOGGER.info("", e);
		}
		
		_source = null;
	}
	
	/**
	 * Gets the single instance of DatabaseFactory.
	 * @return single instance of DatabaseFactory
	 */
	public static DatabaseFactory getInstance()
	{
		synchronized (DatabaseFactory.class)
		{
			if (_instance == null)
			{
				_instance = new DatabaseFactory();
			}
		}
		return _instance;
	}
	
	/**
	 * Gets the connection.
	 * @return the connection
	 */
	public Connection getConnection()
	{
		Connection con = null;
		while (con == null)
		{
			try
			{
				con = _source.getConnection();
				ThreadPoolManager.getInstance().schedule(new ConnectionCloser(con, new RuntimeException()), BotConfig.CONNECTION_CLOSE_TIME);
			}
			catch (SQLException e)
			{
				LOGGER.warn("getConnection() failed, trying again", e);
			}
		}
		return con;
	}
	
	/**
	 * The Class ConnectionCloser.
	 */
	private static class ConnectionCloser implements Runnable
	{
		private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionCloser.class);
		
		/** The connection. */
		private final Connection c;
		
		/** The exception. */
		private final RuntimeException exp;
		
		/**
		 * Instantiates a new connection closer.
		 * @param con the con
		 * @param e the e
		 */
		public ConnectionCloser(Connection con, RuntimeException e)
		{
			c = con;
			exp = e;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (!c.isClosed())
				{
					LOGGER.warn("Unclosed connection! Trace: {}", exp.getStackTrace()[1], exp);
				}
			}
			catch (SQLException e)
			{
				LOGGER.warn("", e);
			}
		}
	}
	
	/**
	 * Gets the busy connection count.
	 * @return the busy connection count
	 * @throws SQLException the SQL exception
	 */
	public int getBusyConnectionCount() throws SQLException
	{
		return _source.getNumBusyConnectionsDefaultUser();
	}
	
	/**
	 * Gets the idle connection count.
	 * @return the idle connection count
	 * @throws SQLException the SQL exception
	 */
	public int getIdleConnectionCount() throws SQLException
	{
		return _source.getNumIdleConnectionsDefaultUser();
	}
	
}
