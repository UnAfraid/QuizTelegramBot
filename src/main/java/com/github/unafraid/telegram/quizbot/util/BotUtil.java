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
package com.github.unafraid.telegram.quizbot.util;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.telegram.telegrambots.api.methods.ActionType;
import org.telegram.telegrambots.api.methods.send.SendChatAction;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.github.unafraid.telegram.quizbot.bothandlers.ChannelBot;
import com.github.unafraid.telegram.quizbot.handlers.ICommandHandler;

/**
 * @author UnAfraid
 */
public class BotUtil
{
	public static int parseInt(String intValue, int defaultValue)
	{
		try
		{
			return Integer.parseInt(intValue);
		}
		catch (Exception e)
		{
			return defaultValue;
		}
	}
	
	public static void sendAction(ChannelBot bot, Message message, ActionType action) throws TelegramApiException
	{
		final SendChatAction sendAction = new SendChatAction();
		sendAction.setChatId(Long.toString(message.getChat().getId()));
		sendAction.setAction(action);
		bot.sendChatAction(sendAction);
	}
	
	public static void sendUsage(ChannelBot bot, Message message, ICommandHandler handler) throws TelegramApiException
	{
		final SendMessage msg = new SendMessage();
		msg.setChatId(Long.toString(message.getChat().getId()));
		msg.setText(handler.getUsage());
		bot.sendMessage(msg);
	}
	
	public static void sendMessage(ChannelBot bot, Message message, String text, boolean replyToMessage, boolean useMarkDown, ReplyKeyboard replayMarkup) throws TelegramApiException
	{
		final SendMessage msg = new SendMessage();
		msg.setChatId(Long.toString(message.getChat().getId()));
		msg.setText(text);
		msg.enableMarkdown(useMarkDown);
		if (replyToMessage)
		{
			msg.setReplyToMessageId(message.getMessageId());
		}
		if (replayMarkup != null)
		{
			msg.setReplyMarkup(replayMarkup);
		}
		bot.sendMessage(msg);
	}
	
	public static void sendPhoto(ChannelBot bot, Message message, String caption, String fileName, InputStream dataStream) throws TelegramApiException
	{
		final SendPhoto photo = new SendPhoto();
		photo.setChatId(Long.toString(message.getChat().getId()));
		photo.setPhoto(fileName);
		photo.setNewPhoto(fileName, dataStream);
		if (caption != null)
		{
			photo.setCaption(caption);
		}
		photo.setReplyToMessageId(message.getMessageId());
		bot.sendPhoto(photo);
	}
	
	public static void sendPhoto(ChannelBot bot, Message message, String caption, boolean replyToMessage, File file) throws TelegramApiException
	{
		final SendPhoto photo = new SendPhoto();
		photo.setChatId(Long.toString(message.getChat().getId()));
		photo.setNewPhoto(file);
		if (caption != null)
		{
			photo.setCaption(caption);
		}
		if (replyToMessage)
		{
			photo.setReplyToMessageId(message.getMessageId());
		}
		bot.sendPhoto(photo);
	}
	
	/**
	 * Format the given date on the given format
	 * @param date : the date to format.
	 * @param format : the format to correct by.
	 * @return a string representation of the formatted date.
	 */
	public static String formatDate(Date date, String format)
	{
		if (date == null)
		{
			return null;
		}
		final DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}
	
	/**
	 * @param string
	 * @return
	 */
	public static boolean isDigit(String string)
	{
		try
		{
			Integer.parseInt(string);
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}
}
