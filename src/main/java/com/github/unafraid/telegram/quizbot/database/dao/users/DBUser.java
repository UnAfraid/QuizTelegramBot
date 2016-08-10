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
package com.github.unafraid.telegram.quizbot.database.dao.users;

/**
 * @author UnAfraid
 */
public class DBUser
{
	private int _id;
	private String _name;
	private int _level;
	
	public DBUser(int id, String name, int level)
	{
		_id = id;
		_name = name;
		_level = level;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if (!(object instanceof DBUser))
		{
			return false;
		}
		else if (object == this)
		{
			return true;
		}
		
		final DBUser user = (DBUser) object;
		return (user.getId() == _id) && (user.getLevel() == _level) && (((user.getName() == null) && (_name == null)) || (user.getName().equalsIgnoreCase(_name)));
	}
}
