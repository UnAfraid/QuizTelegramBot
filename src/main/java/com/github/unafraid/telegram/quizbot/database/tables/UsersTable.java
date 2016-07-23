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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.github.unafraid.telegram.quizbot.database.tables.model.DBUser;

/**
 * @author UnAfraid
 */
public class UsersTable extends AbstractDatabaseTable
{
	public static void addUser(DBUser user)
	{
		executeQuery("INSERT INTO users (id, name, `level`) VALUES (?, ?, ?)", ps ->
		{
			ps.setInt(1, user.getId());
			ps.setString(2, user.getName());
			ps.setInt(3, user.getLevel());
		});
	}
	
	public static void removeUser(DBUser user)
	{
		executeQuery("DELETE FROM users WHERE id = ?", ps -> ps.setInt(1, user.getId()));
	}
	
	public static void updateUser(DBUser user)
	{
		if (executeUpdateQuery("UPDATE users SET name = ?, `level` = ? WHERE id = ?", ps ->
		{
			ps.setString(1, user.getName());
			ps.setInt(2, user.getLevel());
			ps.setInt(3, user.getId());
		}) < 1)
		{
			executeQuery("UPDATE users SET id = ?, `level` = ? WHERE name = ?", ps ->
			{
				ps.setInt(1, user.getId());
				ps.setInt(2, user.getLevel());
				ps.setString(3, user.getName());
			});
		}
	}
	
	public static DBUser getUser(int id)
	{
		final AtomicReference<DBUser> userRef = new AtomicReference<>(null);
		selectQuery("SELECT * FROM users WHERE id = ?", ps -> ps.setInt(1, id), rs -> userRef.set(new DBUser(rs.getInt("id"), rs.getString("name"), rs.getInt("level"))));
		
		return userRef.get();
	}
	
	public static Map<Integer, DBUser> getUsers()
	{
		final Map<Integer, DBUser> users = new LinkedHashMap<>();
		selectQuery("SELECT * FROM users", rs ->
		{
			final DBUser user = new DBUser(rs.getInt("id"), rs.getString("name"), rs.getInt("level"));
			users.put(user.getId(), user);
		});
		return users;
	}
}
