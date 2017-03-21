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
import com.github.unafraid.telegram.quizbot.handlers.ICommandHandler;
import com.github.unafraid.telegram.quizbot.util.BotUtil;

/**
 * @author UnAfraid
 */
public final class WhoAmI implements ICommandHandler
{
	@Override
	public String getCommand()
	{
		return "/whoami";
	}
	
	@Override
	public String getUsage()
	{
		return "/whoami";
	}
	
	@Override
	public String getDescription()
	{
		return "Shows information for the user who types the command";
	}
	
	@Override
	public void onMessage(ChannelBot bot, Message message, int updateId, List<String> args) throws TelegramApiException
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("Your id: ").append(message.getFrom().getId()).append(System.lineSeparator());
		sb.append("Name: ").append(message.getFrom().getFirstName()).append(System.lineSeparator());
		if (message.getFrom().getUserName() != null)
		{
			sb.append("Username: @").append(message.getFrom().getUserName()).append(System.lineSeparator());
		}
		sb.append("Chat Type: ").append(message.getChat().isGroupChat() ? "Group Chat" : message.getChat().isSuperGroupChat() ? "Super Group Chat" : message.getChat().isChannelChat() ? "Channel Chat" : message.getChat().isUserChat() ? "User Chat" : "No way!?").append(System.lineSeparator());
		if (message.getChat().getId() < 0)
		{
			sb.append("Group Id: ").append(message.getChat().getId()).append(System.lineSeparator());
			sb.append("Group Name: ").append(message.getChat().getTitle()).append(System.lineSeparator());
		}
		BotUtil.sendMessage(bot, message, sb.toString(), true, false, null);
	}
}
