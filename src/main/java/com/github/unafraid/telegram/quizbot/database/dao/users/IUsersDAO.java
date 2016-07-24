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
public interface IUsersDAO
{
	public boolean create(DBUser user);
	
	public boolean update(DBUser user);
	
	public boolean delete(DBUser user);
	
	public DBUser findById(int id);
	
	public DBUser findByUsername(String username);
	
	public List<DBUser> findAll();
}
