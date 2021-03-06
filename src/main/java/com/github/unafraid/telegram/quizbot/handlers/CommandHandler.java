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
package com.github.unafraid.telegram.quizbot.handlers;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.unafraid.telegram.quizbot.handlers.commands.HelpHandler;
import com.github.unafraid.telegram.quizbot.handlers.commands.QuizCommandHandler;
import com.github.unafraid.telegram.quizbot.handlers.commands.ReloadHandler;
import com.github.unafraid.telegram.quizbot.handlers.commands.ResolveHandler;
import com.github.unafraid.telegram.quizbot.handlers.commands.RestartHandler;
import com.github.unafraid.telegram.quizbot.handlers.commands.ShutdownHandler;
import com.github.unafraid.telegram.quizbot.handlers.commands.StartHandler;
import com.github.unafraid.telegram.quizbot.handlers.commands.UsersHandler;
import com.github.unafraid.telegram.quizbot.handlers.commands.WhoAmI;

/**
 * @author UnAfraid
 */
public final class CommandHandler
{
	private final Map<String, ICommandHandler> _handlers = new ConcurrentHashMap<>();
	
	protected CommandHandler()
	{
		// General
		addHandler(new HelpHandler());
		addHandler(new StartHandler());
		
		// System
		addHandler(new UsersHandler());
		addHandler(new ReloadHandler());
		addHandler(new ResolveHandler());
		addHandler(new RestartHandler());
		addHandler(new ShutdownHandler());
		addHandler(new WhoAmI());
		
		// Quiz
		addHandler(new QuizCommandHandler());
	}
	
	public void addHandler(ICommandHandler handler)
	{
		_handlers.put(handler.getCommand(), handler);
	}
	
	public ICommandHandler getHandler(String command)
	{
		return _handlers.get(command);
	}
	
	public Collection<ICommandHandler> getHandlers()
	{
		return _handlers.values();
	}
	
	public static CommandHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CommandHandler INSTANCE = new CommandHandler();
	}
}
