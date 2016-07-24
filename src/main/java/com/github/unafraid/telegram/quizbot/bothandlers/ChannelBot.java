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
package com.github.unafraid.telegram.quizbot.bothandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import com.github.unafraid.telegram.quizbot.handlers.CommandHandler;
import com.github.unafraid.telegram.quizbot.handlers.ICommandHandler;
import com.github.unafraid.telegram.quizbot.handlers.IMessageHandler;
import com.github.unafraid.telegram.quizbot.handlers.MessageHandler;
import com.github.unafraid.telegram.quizbot.handlers.commands.UsersHandler;
import com.github.unafraid.telegram.quizbot.util.BotUtil;

/**
 * @author UnAfraid
 */
public class ChannelBot extends TelegramLongPollingBot
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelBot.class);
	private static final Pattern COMMAND_ARGS_PATTERN = Pattern.compile("\"([^\"]*)\"|([^\\s]+)");
	
	private final String _token;
	private final String _username;
	
	public ChannelBot(String token, String username)
	{
		_token = token;
		_username = username;
	}
	
	@Override
	public void onUpdateReceived(Update update)
	{
		try
		{
			final Message message = update.getMessage();
			if ((message != null) && message.hasText())
			{
				handleIncomingMessage(update.getUpdateId(), message);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to handle incomming update", e);
		}
	}
	
	private void handleIncomingMessage(int updateId, Message message)
	{
		String text = message.getText();
		if (text == null)
		{
			return;
		}
		
		if (text.startsWith("@" + _username + " "))
		{
			text = '/' + text.substring(("@" + _username + " ").length());
		}
		else if (text.contains("@" + _username))
		{
			text = text.replaceAll("@" + _username, "");
			if (text.charAt(0) != '/')
			{
				text = '/' + text;
			}
		}
		
		final Matcher matcher = COMMAND_ARGS_PATTERN.matcher(text);
		if (matcher.find())
		{
			final String command = matcher.group();
			final List<String> args = new ArrayList<>(matcher.groupCount());
			String arg;
			
			while (matcher.find())
			{
				arg = matcher.group(1);
				if (arg == null)
				{
					arg = matcher.group(0);
				}
				
				args.add(arg);
			}
			
			final ICommandHandler handler = CommandHandler.getInstance().getHandler(command);
			if (handler != null)
			{
				try
				{
					final int id = message.getFrom().getId();
					if (!UsersHandler.validate(id, handler.getRequiredAccessLevel()))
					{
						BotUtil.sendMessage(this, message, message.getFrom().getUserName() + ": You are not authorized to use this function!", true, false, null);
						return;
					}
					
					handler.onMessage(this, message, updateId, args);
				}
				catch (Exception e)
				{
					LOGGER.warn("Exception caught on handler: {}, message: {}", handler.getClass().getSimpleName(), message, e);
				}
			}
			else
			{
				for (IMessageHandler messageHandler : MessageHandler.getInstance().getHandlers())
				{
					try
					{
						if (messageHandler.onMessage(this, message))
						{
							break;
						}
					}
					catch (Exception e)
					{
						LOGGER.warn("Exception caught on handler: {}, message: {}", messageHandler.getClass().getSimpleName(), message, e);
					}
				}
			}
		}
		else
		{
			for (IMessageHandler handler : MessageHandler.getInstance().getHandlers())
			{
				try
				{
					if (handler.onMessage(this, message))
					{
						break;
					}
				}
				catch (Exception e)
				{
					LOGGER.warn("Exception caught on handler: {}, message: {}", handler.getClass().getSimpleName(), message, e);
				}
			}
		}
	}
	
	@Override
	public String getBotUsername()
	{
		return _username;
	}
	
	@Override
	public String getBotToken()
	{
		return _token;
	}
}
