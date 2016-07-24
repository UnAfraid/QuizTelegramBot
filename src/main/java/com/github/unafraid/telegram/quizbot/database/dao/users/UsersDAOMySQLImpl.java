/*
 * Copyright (C) 2004-2016 L2J Unity
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
package com.github.unafraid.telegram.quizbot.database.dao.users;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.unafraid.telegram.quizbot.database.DatabaseFactory;

/**
 * @author UnAfraid
 */
public class UsersDAOMySQLImpl implements IUsersDAO
{
	private static final Logger LOGGER = LoggerFactory.getLogger(UsersDAOMySQLImpl.class);
	
	protected UsersDAOMySQLImpl()
	{
	}
	
	@Override
	public boolean create(DBUser user)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO users (id, name, `level`) VALUES (?, ?, ?)"))
		{
			ps.setInt(1, user.getId());
			ps.setString(2, user.getName());
			ps.setInt(3, user.getLevel());
			ps.execute();
			return true;
		}
		catch (SQLException e)
		{
			LOGGER.warn("Failed to create user: {}", user, e);
		}
		return false;
	}
	
	@Override
	public boolean update(DBUser user)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE users SET name = ?, `level` = ? WHERE id = ?"))
		{
			ps.setString(1, user.getName());
			ps.setInt(2, user.getLevel());
			ps.setInt(3, user.getId());
			ps.execute();
			return true;
		}
		catch (SQLException e)
		{
			LOGGER.warn("Failed to update user: {}", user, e);
		}
		return false;
	}
	
	@Override
	public boolean delete(DBUser user)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE id = ?"))
		{
			ps.setInt(1, user.getId());
			ps.execute();
			return true;
		}
		catch (SQLException e)
		{
			LOGGER.warn("Failed to delete user: {}", user, e);
		}
		return false;
	}
	
	@Override
	public DBUser findById(int id)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE id = ?"))
		{
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return new DBUser(rs.getInt("id"), rs.getString("name"), rs.getInt("level"));
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.warn("Failed to find userId: {}", id, e);
		}
		return null;
	}
	
	@Override
	public DBUser findByUsername(String username)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE name = ?"))
		{
			ps.setString(1, username);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return new DBUser(rs.getInt("id"), rs.getString("name"), rs.getInt("level"));
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.warn("Failed to find userName: {}", username, e);
		}
		return null;
	}
	
	@Override
	public List<DBUser> findAll()
	{
		final List<DBUser> users = new ArrayList<>();
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			Statement st = con.createStatement())
		{
			try (ResultSet rs = st.executeQuery("SELECT * FROM users"))
			{
				while (rs.next())
				{
					users.add(new DBUser(rs.getInt("id"), rs.getString("name"), rs.getInt("level")));
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.warn("Failed to find all users: {}", e);
		}
		return users;
	}
}
