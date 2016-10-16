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
package com.github.unafraid.telegram.quizbot.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.l2junity.commons.util.IXmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.github.unafraid.telegram.quizbot.util.StatsSet;

/**
 * @author UnAfraid
 */
public class HandlersData implements IXmlReader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(HandlersData.class);
	private final List<HandlerEntry> _handlerEntries = new ArrayList<>();
	
	protected HandlersData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		final String token = System.getenv("BOT_TOKEN");
		final String username = System.getenv("BOT_USERNAME");
		final String handler = System.getenv("BOT_HANDLER");
		final File handlersFile = new File("config/handlers.xml");
		if (handlersFile.exists())
		{
			parseFile(new File("config/handlers.xml"));
		}
		else
		{
			if (token != null && username != null && handler != null)
			{
				final StatsSet set = new StatsSet();
				set.set("primary", true);
				set.set("enabled", true);
				set.set("token", token);
				set.set("username", username);
				set.set("handler", handler);
				_handlerEntries.add(new HandlerEntry(set));
			}
		}
		LOGGER.info("Loaded: {} handlers", _handlerEntries.size());
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "bot", botNode ->
		{
			final StatsSet set = new StatsSet(parseAttributes(botNode));
			forEach(botNode, IXmlReader::isNode, innerNode ->
			{
				set.set(innerNode.getNodeName(), innerNode.getTextContent());
			});
			_handlerEntries.add(new HandlerEntry(set));
		}));
	}
	
	public HandlerEntry getPrimaryHandler()
	{
		return _handlerEntries.stream().filter(HandlerEntry::isPrimary).findFirst().orElse(null);
	}
	
	public List<HandlerEntry> getHandlers()
	{
		return _handlerEntries;
	}
	
	public static class HandlerEntry
	{
		private final boolean _primary;
		private final boolean _enabled;
		private final String _token;
		private final String _username;
		private final String _handler;
		
		public HandlerEntry(StatsSet set)
		{
			_primary = set.getBoolean("primary", false);
			_enabled = set.getBoolean("enabled", false);
			_token = set.getString("token");
			_username = set.getString("username");
			_handler = set.getString("handler");
		}
		
		public boolean isPrimary()
		{
			return _primary;
		}
		
		public boolean isEnabled()
		{
			return _enabled;
		}
		
		public String getToken()
		{
			return _token;
		}
		
		public String getUsername()
		{
			return _username;
		}
		
		public String getHandler()
		{
			return _handler;
		}
	}
	
	public static HandlersData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HandlersData INSTANCE = new HandlersData();
	}
}
