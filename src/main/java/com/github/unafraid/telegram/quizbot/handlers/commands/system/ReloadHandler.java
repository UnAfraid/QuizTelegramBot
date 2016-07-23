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
package com.github.unafraid.telegram.quizbot.handlers.commands.system;

import java.util.List;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.objects.Message;

import com.github.unafraid.telegram.quizbot.BotConfig;
import com.github.unafraid.telegram.quizbot.bothandlers.ChannelBot;
import com.github.unafraid.telegram.quizbot.data.QuizData;
import com.github.unafraid.telegram.quizbot.database.tables.UsersTable;
import com.github.unafraid.telegram.quizbot.handlers.commands.ICommandHandler;
import com.github.unafraid.telegram.quizbot.util.BotUtil;

/**
 * @author UnAfraid
 */
public final class ReloadHandler implements ICommandHandler
{
	@Override
	public String getCommand()
	{
		return "/reload";
	}
	
	@Override
	public String getUsage()
	{
		return "/reload <config|authorizedUsers>";
	}
	
	@Override
	public String getDescription()
	{
		return "Reloads bot configuration";
	}
	
	@Override
	public int getRequiredAccessLevel()
	{
		return 5;
	}
	
	@Override
	public void onMessage(ChannelBot bot, Message message, int updateId, List<String> args) throws TelegramApiException
	{
		if (args.isEmpty())
		{
			BotUtil.sendUsage(bot, message, this);
			return;
		}
		
		switch (args.get(0))
		{
			case "config":
			{
				BotConfig.load();
				BotUtil.sendMessage(bot, message, "Reloaded configuration successfully!", true, false, null);
				break;
			}
			case "users":
			{
				UsersHandler.getUsers().clear();
				UsersHandler.getUsers().putAll(UsersTable.getUsers());
				BotUtil.sendMessage(bot, message, "Reloaded all authorized users from database!", false, false, null);
				break;
			}
			case "quiz":
			{
				QuizData.getInstance().load();
				BotUtil.sendMessage(bot, message, "Reloaded all quiz data!", false, false, null);
				break;
			}
			default:
			{
				BotUtil.sendUsage(bot, message, this);
				break;
			}
		}
	}
}
