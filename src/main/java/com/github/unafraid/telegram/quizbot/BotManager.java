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
package com.github.unafraid.telegram.quizbot;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import com.github.unafraid.telegram.quizbot.bothandlers.ChannelBot;
import com.github.unafraid.telegram.quizbot.data.HandlersData;
import com.github.unafraid.telegram.quizbot.data.HandlersData.HandlerEntry;

/**
 * @author UnAfraid
 */
public final class BotManager
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BotManager.class);
	
	private TelegramLongPollingBot _primaryBot;
	private final List<TelegramLongPollingBot> _bots = new ArrayList<>();
	
	protected BotManager()
	{
		final TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
		for (HandlerEntry entry : HandlersData.getInstance().getHandlers())
		{
			if (!entry.isEnabled())
			{
				LOGGER.info("Skipping disabled bot handler: {} - {}", entry.getHandler(), entry.getUsername());
				continue;
			}
			
			try
			{
				final TelegramLongPollingBot bot = initializeBotHandler(entry);
				telegramBotsApi.registerBot(bot);
				
				if (entry.isPrimary() && (_primaryBot == null))
				{
					_primaryBot = bot;
					LOGGER.info("Initialized primary bot handler: {} - {}", bot.getClass().getSimpleName(), entry.getUsername());
				}
				else
				{
					LOGGER.info("Initialized bot handler: {} - {}", bot.getClass().getSimpleName(), entry.getUsername());
				}
				_bots.add(bot);
			}
			catch (TelegramApiRequestException e)
			{
				LOGGER.warn("Failed to initialize bot handler: {} response: {}", entry.getHandler(), e.getApiResponse(), e);
			}
			catch (Exception e)
			{
				LOGGER.warn("Failed to initialize bot handler: {}", entry.getHandler(), e);
			}
		}
		
		if (_primaryBot == null)
		{
			throw new NullPointerException("No primary bot was loaded!");
		}
	}
	
	private TelegramLongPollingBot initializeBotHandler(HandlerEntry entry)
	{
		switch (entry.getHandler().toLowerCase())
		{
			case "channelbot":
			{
				return new ChannelBot(entry.getToken(), entry.getUsername());
			}
			default:
			{
				throw new IllegalStateException("Not handled handler type: " + entry.getHandler());
			}
		}
	}
	
	public TelegramLongPollingBot getPrimaryBot()
	{
		return _primaryBot;
	}
	
	public List<TelegramLongPollingBot> getBots()
	{
		return _bots;
	}
	
	public void shutdown()
	{
	}
	
	public static BotManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BotManager INSTANCE = new BotManager();
	}
}
