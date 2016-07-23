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
package com.github.unafraid.telegram.quizbot.database.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.unafraid.telegram.quizbot.database.DatabaseFactory;
import com.github.unafraid.telegram.quizbot.util.ExConsumer;

/**
 * @author UnAfraid
 */
public class AbstractDatabaseTable
{
	static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatabaseTable.class);
	
	protected static void selectQuery(String query, ExConsumer<ResultSet> resultSetConsumer)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			Statement st = con.createStatement())
		{
			try (ResultSet rset = st.executeQuery(query))
			{
				while (rset.next())
				{
					resultSetConsumer.accept(rset);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("Exception while selecting query: {}", query, e);
		}
	}
	
	protected static void selectQuery(String query, ExConsumer<PreparedStatement> statementConsumer, ExConsumer<ResultSet> resultSetConsumer)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			statementConsumer.accept(ps);
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					resultSetConsumer.accept(rset);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("Exception while selecting query: {}", query, e);
		}
	}
	
	protected static void executeQueryGenerateKey(String query, ExConsumer<PreparedStatement> statementConsumer, ExConsumer<ResultSet> resultSetConsumer)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
		{
			statementConsumer.accept(ps);
			ps.execute();
			try (ResultSet rset = ps.getGeneratedKeys())
			{
				if (rset.next())
				{
					resultSetConsumer.accept(rset);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("Exception while selecting query: {}", query, e);
		}
	}
	
	protected static void executeQuery(String query, ExConsumer<PreparedStatement> statementConsumer)
	{
		executeUpdateQuery(query, statementConsumer);
	}
	
	protected static int executeUpdateQuery(String query, ExConsumer<PreparedStatement> statementConsumer)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			statementConsumer.accept(ps);
			return ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.warn("Exception while executing query: {}", query, e);
			return -1;
		}
	}
}
