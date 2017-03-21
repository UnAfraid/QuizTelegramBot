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
package com.github.unafraid.telegram.quizbot.handlers.commands;

import java.util.List;

import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.github.unafraid.telegram.quizbot.bothandlers.ChannelBot;
import com.github.unafraid.telegram.quizbot.handlers.CommandHandler;
import com.github.unafraid.telegram.quizbot.handlers.ICommandHandler;
import com.github.unafraid.telegram.quizbot.util.BotUtil;

/**
 * @author UnAfraid
 */
public final class HelpHandler implements ICommandHandler
{
	@Override
	public String getCommand()
	{
		return "/help";
	}
	
	@Override
	public String getUsage()
	{
		return "/help [command]";
	}
	
	@Override
	public String getDescription()
	{
		return "Shows help for all or specific command";
	}
	
	@Override
	public void onMessage(ChannelBot bot, Message message, int updateId, List<String> args) throws TelegramApiException
	{
		final int id = message.getFrom().getId();
		if (args.isEmpty())
		{
			final StringBuilder sb = new StringBuilder();
			sb.append("Public commands:").append(System.lineSeparator());
			CommandHandler.getInstance().getHandlers().stream().filter(handler -> handler.getRequiredAccessLevel() == 0).forEach(handler ->
			{
				sb.append(handler.getCommand()).append(" - ").append(handler.getDescription()).append(System.lineSeparator());
				
			});
			if (UsersHandler.validate(id, 1))
			{
				sb.append(System.lineSeparator()).append("Admin commands:").append(System.lineSeparator());
				CommandHandler.getInstance().getHandlers().stream().filter(handler -> (handler.getRequiredAccessLevel() > 0) && UsersHandler.validate(id, handler.getRequiredAccessLevel())).forEach(handler ->
				{
					sb.append(handler.getCommand()).append(" - ").append(handler.getDescription()).append(System.lineSeparator());
				});
			}
			BotUtil.sendMessage(bot, message, sb.toString(), true, false, null);
			return;
		}
		
		String command = args.get(0);
		if (command.charAt(0) != '/')
		{
			command = '/' + command;
		}
		final ICommandHandler handler = CommandHandler.getInstance().getHandler(command);
		if (handler == null)
		{
			BotUtil.sendMessage(bot, message, "Unknown command.", false, false, null);
			return;
		}
		
		if (UsersHandler.validate(id, handler.getRequiredAccessLevel()))
		{
			BotUtil.sendMessage(bot, message, "Usage:" + System.lineSeparator() + handler.getUsage(), true, false, null);
		}
	}
}
