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
import com.github.unafraid.telegram.quizbot.database.dao.users.DBUser;
import com.github.unafraid.telegram.quizbot.database.dao.users.UsersFactory;
import com.github.unafraid.telegram.quizbot.handlers.ICommandHandler;
import com.github.unafraid.telegram.quizbot.util.BotUtil;

/**
 * @author UnAfraid
 */
public final class StartHandler implements ICommandHandler
{
	@Override
	public String getCommand()
	{
		return "/start";
	}
	
	@Override
	public String getUsage()
	{
		return "/start";
	}
	
	@Override
	public String getDescription()
	{
		return "Shows greetings message";
	}
	
	@Override
	public void onMessage(ChannelBot bot, Message message, int updateId, List<String> args) throws TelegramApiException
	{
		if (UsersFactory.getInstance().findAll().isEmpty())
		{
			UsersFactory.getInstance().create(new DBUser(message.getFrom().getId(), message.getFrom().getUserName(), 10));
			BotUtil.sendMessage(bot, message, "Hello, " + message.getFrom().getUserName() + " since you're the first who wrote me, you're my master!", true, false, null);
		}
		else
		{
			BotUtil.sendMessage(bot, message, "Hello, i am Quiz Telegram bot, if you want to know what i can do type /start", true, false, null);
		}
	}
}
