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
public final class UsersHandler implements ICommandHandler
{
	public UsersHandler()
	{
	}
	
	@Override
	public String getCommand()
	{
		return "/users";
	}
	
	@Override
	public String getUsage()
	{
		return "/users add <id> <username> <level>|changeLevel <id> <level>|remove <id>|set_id <name> <id>|list";
	}
	
	@Override
	public String getDescription()
	{
		return "Manipulates authorized users";
	}
	
	@Override
	public int getRequiredAccessLevel()
	{
		return 7;
	}
	
	@Override
	public void onMessage(ChannelBot bot, Message message, int updateId, List<String> args) throws TelegramApiException
	{
		if (args.isEmpty())
		{
			BotUtil.sendUsage(bot, message, this);
			return;
		}
		
		final String command = args.get(0);
		switch (command)
		{
			case "add":
			{
				if (args.size() < 4)
				{
					BotUtil.sendMessage(bot, message, "/users add <id> <name> <level>", false, false, null);
					return;
				}
				
				final int id = BotUtil.parseInt(args.get(1), -1);
				if (id == -1)
				{
					BotUtil.sendMessage(bot, message, "invalid id specified", false, false, null);
					return;
				}
				String username = args.get(2);
				int level = BotUtil.parseInt(args.get(3), 0);
				if (username.charAt(0) == '@')
				{
					username = username.substring(1);
				}
				else if (level <= 0)
				{
					BotUtil.sendMessage(bot, message, "/users add <id> <name> <level>", false, false, null);
					return;
				}
				
				if (UsersFactory.getInstance().findById(id) != null)
				{
					BotUtil.sendMessage(bot, message, username + " is already authorized use \"/users changeLevel\" instead!", false, false, null);
					break;
				}
				
				final DBUser user = new DBUser(id, username, level);
				UsersFactory.getInstance().create(user);
				BotUtil.sendMessage(bot, message, username + " is now authorized!", true, false, null);
				break;
			}
			case "changeLevel":
			{
				if (args.size() < 3)
				{
					BotUtil.sendMessage(bot, message, "/users changeLevel <id> <level>", false, false, null);
					return;
				}
				
				final int id = BotUtil.parseInt(args.get(1), -1);
				if (id == -1)
				{
					BotUtil.sendMessage(bot, message, "invalid id specified", false, false, null);
					return;
				}
				
				int level = BotUtil.parseInt(args.get(2), 0);
				if (level <= 0)
				{
					BotUtil.sendMessage(bot, message, "/users changeLevel <id> <level>", false, false, null);
					return;
				}
				
				final DBUser user = UsersFactory.getInstance().findById(id);
				if (user == null)
				{
					BotUtil.sendMessage(bot, message, "There is no user with such id", false, false, null);
					return;
				}
				
				user.setLevel(level);
				UsersFactory.getInstance().update(user);
				BotUtil.sendMessage(bot, message, user.getName() + " has been updated!", false, false, null);
				break;
			}
			case "remove":
			{
				if (args.size() < 2)
				{
					BotUtil.sendMessage(bot, message, "/users remove <id>", false, false, null);
					return;
				}
				
				final int id = BotUtil.parseInt(args.get(1), -1);
				if (id == -1)
				{
					BotUtil.sendMessage(bot, message, "invalid id specified", false, false, null);
					return;
				}
				
				final DBUser user = UsersFactory.getInstance().findById(id);
				if (user == null)
				{
					BotUtil.sendMessage(bot, message, "There is no user with such id", false, false, null);
					break;
				}
				UsersFactory.getInstance().delete(user);
				BotUtil.sendMessage(bot, message, user.getName() + " is no longer authorized!", true, false, null);
				break;
			}
			case "list":
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("Authorized users:").append(System.lineSeparator());
				for (DBUser user : UsersFactory.getInstance().findAll())
				{
					sb.append(" - ").append(user.getId()).append(" @").append(user.getName()).append(" level: ").append(user.getLevel()).append(System.lineSeparator());
				}
				BotUtil.sendMessage(bot, message, sb.toString(), true, false, null);
				break;
			}
			default:
			{
				BotUtil.sendUsage(bot, message, this);
				break;
			}
		}
	}
	
	public static boolean validate(int id, int level)
	{
		if (level == 0)
		{
			return true;
		}
		
		final DBUser user = UsersFactory.getInstance().findById(id);
		return (user != null) && (user.getLevel() >= level);
	}
}
