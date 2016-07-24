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

/**
 * @author UnAfraid
 */
public class UsersFactory implements IUsersDAO
{
	private final IUsersDAO _impl = new UsersDAOMySQLImpl();
	
	protected UsersFactory()
	{
	}
	
	@Override
	public boolean create(DBUser user)
	{
		return _impl.create(user);
	}
	
	@Override
	public boolean update(DBUser user)
	{
		return _impl.update(user);
	}
	
	@Override
	public boolean delete(DBUser user)
	{
		return _impl.delete(user);
	}
	
	@Override
	public DBUser findById(int id)
	{
		return _impl.findById(id);
	}
	
	@Override
	public DBUser findByUsername(String username)
	{
		return _impl.findByUsername(username);
	}
	
	@Override
	public List<DBUser> findAll()
	{
		return _impl.findAll();
	}
	
	public static UsersFactory getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final UsersFactory INSTANCE = new UsersFactory();
	}
}
