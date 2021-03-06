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
package com.github.unafraid.telegram.quizbot.handlers.commands;

import java.util.List;

import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.github.unafraid.telegram.quizbot.bothandlers.ChannelBot;
import com.github.unafraid.telegram.quizbot.handlers.ICommandHandler;
import com.github.unafraid.telegram.quizbot.handlers.QuizHandler;
import com.github.unafraid.telegram.quizbot.util.BotUtil;

/**
 * @author UnAfraid
 */
public class QuizCommandHandler implements ICommandHandler
{
	@Override
	public String getCommand()
	{
		return "/quiz";
	}
	
	@Override
	public String getUsage()
	{
		return "/quiz <start> or <stop> or <skip> or <report>";
	}
	
	@Override
	public String getDescription()
	{
		return "Manages quiz status";
	}
	
	@Override
	public int getRequiredAccessLevel()
	{
		return 3;
	}
	
	@Override
	public void onMessage(ChannelBot bot, Message message, int updateId, List<String> args) throws TelegramApiException
	{
		if (args.isEmpty())
		{
			BotUtil.sendUsage(bot, message, this);
			return;
		}
		
		final String cmd = args.get(0);
		switch (cmd)
		{
			case "start":
			{
				QuizHandler.getInstance().startQuiz(bot, message);
				break;
			}
			case "stop":
			{
				QuizHandler.getInstance().stopQuiz(bot, message);
				break;
			}
			case "skip":
			{
				QuizHandler.getInstance().quizSkip(bot, message);
				break;
			}
			case "report":
			{
				QuizHandler.getInstance().quizReport(bot, message);
				break;
			}
		}
	}
}
