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

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.objects.Message;

import com.github.unafraid.telegram.quizbot.bothandlers.ChannelBot;

/**
 * @author UnAfraid
 */
public interface ICommandHandler
{
	String getCommand();
	
	String getUsage();
	
	String getDescription();
	
	default int getRequiredAccessLevel()
	{
		return 0;
	}
	
	void onMessage(ChannelBot bot, Message message, int updateId, List<String> args) throws TelegramApiException;
}
