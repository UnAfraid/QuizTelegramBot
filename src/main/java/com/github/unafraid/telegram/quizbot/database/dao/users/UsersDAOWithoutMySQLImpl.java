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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author UnAfraid
 */
public class UsersDAOWithoutMySQLImpl implements IUsersDAO
{
	private final Set<DBUser> _users = ConcurrentHashMap.newKeySet();
	
	protected UsersDAOWithoutMySQLImpl()
	{
	}
	
	@Override
	public boolean create(DBUser user)
	{
		return _users.add(user);
	}
	
	@Override
	public boolean update(DBUser user)
	{
		return _users.contains(user);
	}
	
	@Override
	public boolean delete(DBUser user)
	{
		return _users.remove(user);
	}
	
	@Override
	public DBUser findById(int id)
	{
		return _users.stream().filter(user -> user.getId() == id).findFirst().orElse(null);
	}
	
	@Override
	public DBUser findByUsername(String username)
	{
		Objects.requireNonNull(username);
		return _users.stream().filter(user -> username.equalsIgnoreCase(user.getName())).findFirst().orElse(null);
	}
	
	@Override
	public List<DBUser> findAll()
	{
		return _users.stream().collect(Collectors.toList());
	}
}
