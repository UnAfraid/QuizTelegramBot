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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.objects.Message;

import com.github.unafraid.telegram.quizbot.bothandlers.ChannelBot;
import com.github.unafraid.telegram.quizbot.database.tables.UsersTable;
import com.github.unafraid.telegram.quizbot.database.tables.model.DBUser;
import com.github.unafraid.telegram.quizbot.handlers.commands.ICommandHandler;
import com.github.unafraid.telegram.quizbot.util.BotUtil;

/**
 * @author UnAfraid
 */
public final class UsersHandler implements ICommandHandler
{
	private static final Map<Integer, DBUser> USERS = new LinkedHashMap<>();
	
	public UsersHandler()
	{
		USERS.putAll(UsersTable.getUsers());
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
				
				if (getUsers().containsKey(id))
				{
					BotUtil.sendMessage(bot, message, username + " is already authorized use \"/users changeLevel\" instead!", false, false, null);
					break;
				}
				
				final DBUser user = new DBUser(id, username, level);
				UsersTable.addUser(user);
				getUsers().put(user.getId(), user);
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
				
				final DBUser user = getUsers().get(id);
				if (user == null)
				{
					BotUtil.sendMessage(bot, message, "There is no user with such id", false, false, null);
					return;
				}
				
				user.setLevel(level);
				UsersTable.updateUser(user);
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
				
				final DBUser user = getUsers().get(id);
				if (user == null)
				{
					BotUtil.sendMessage(bot, message, "There is no user with such id", false, false, null);
					break;
				}
				UsersTable.removeUser(user);
				getUsers().remove(user.getId());
				BotUtil.sendMessage(bot, message, user.getName() + " is no longer authorized!", true, false, null);
				break;
			}
			case "list":
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("Authorized users:").append(System.lineSeparator());
				for (DBUser user : getUsers().values())
				{
					sb.append(" - ").append(user.getId()).append(" @").append(user.getName()).append(" level: ").append(user.getLevel()).append(System.lineSeparator());
				}
				BotUtil.sendMessage(bot, message, sb.toString(), true, false, null);
				break;
			}
			case "reload":
			{
				getUsers().clear();
				getUsers().putAll(UsersTable.getUsers());
				BotUtil.sendMessage(bot, message, "Reloaded all users from database!", false, false, null);
				break;
			}
			case "set_id":
			{
				if (args.size() < 3)
				{
					BotUtil.sendMessage(bot, message, "/users set_id <name> <id>", false, false, null);
					return;
				}
				
				final String name = args.get(1);
				final int id = BotUtil.parseInt(args.get(2), -1);
				if (id == -1)
				{
					BotUtil.sendMessage(bot, message, "invalid id specified", false, false, null);
					return;
				}
				
				final DBUser user = getUsers().values().stream().filter(u -> u.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
				if (user == null)
				{
					BotUtil.sendMessage(bot, message, "There is no user with such name", false, false, null);
					return;
				}
				user.setId(id);
				UsersTable.updateUser(user);
				BotUtil.sendMessage(bot, message, "Updated id for user: " + user.getName(), false, false, null);
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
		final DBUser user = USERS.get(id);
		return (level == 0) || ((user != null) && (user.getLevel() >= level));
	}
	
	public static Map<Integer, DBUser> getUsers()
	{
		return USERS;
	}
	
	public static DBUser getUser(int id)
	{
		return USERS.get(id);
	}
}
