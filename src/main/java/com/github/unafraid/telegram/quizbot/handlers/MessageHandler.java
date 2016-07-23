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

import java.util.ArrayList;
import java.util.List;

import com.github.unafraid.telegram.quizbot.handlers.commands.IMessageHandler;

/**
 * @author UnAfraid
 */
public final class MessageHandler
{
	private final List<IMessageHandler> _handlers = new ArrayList<>();
	
	protected MessageHandler()
	{
		addHandler(QuizHandler.getInstance());
	}
	
	public void addHandler(IMessageHandler handler)
	{
		_handlers.add(handler);
	}
	
	public List<IMessageHandler> getHandlers()
	{
		return _handlers;
	}
	
	public static MessageHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final MessageHandler INSTANCE = new MessageHandler();
	}
}
