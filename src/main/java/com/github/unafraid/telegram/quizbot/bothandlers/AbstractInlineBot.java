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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

/**
 * @author UnAfraid
 * @param <T>
 */
public abstract class AbstractInlineBot<T> extends TelegramLongPollingBot
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInlineBot.class);
	protected static final NumberFormat FORMATTER = NumberFormat.getNumberInstance(Locale.ENGLISH);
	private static final Integer CACHETIME = 86400;
	private static final int SEARCH_RESULTS = 10;
	
	private final String _token;
	private final String _username;
	
	public AbstractInlineBot(String token, String username)
	{
		_token = token;
		_username = username;
	}
	
	@Override
	public void onUpdateReceived(Update update)
	{
		try
		{
			if (update.hasInlineQuery())
			{
				handleIncomingInlineQuery(update.getInlineQuery());
			}
			else if (update.hasMessage() && update.getMessage().isUserMessage())
			{
				try
				{
					sendMessage(getHelpMessage(update.getMessage()));
				}
				catch (TelegramApiException e)
				{
					LOGGER.error("Failed to handle incomming update", e);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to handle incomming update", e);
		}
	}
	
	private void handleIncomingInlineQuery(InlineQuery inlineQuery)
	{
		final String query = inlineQuery.getQuery();
		try
		{
			answerInlineQuery(converteResultsToResponse(inlineQuery, handleSearch(query)));
		}
		catch (TelegramApiException e)
		{
			LOGGER.error("Failed to read query:", e);
		}
	}
	
	protected abstract List<T> handleSearch(String query);
	
	public static <T> List<T> handleSearch(Stream<T> searchItems, String value, Function<T, Integer> idFunction, Function<T, String> nameFunction)
	{
		final AtomicInteger displayed = new AtomicInteger();
		final Set<Integer> added = new HashSet<>(SEARCH_RESULTS);
		return searchItems.filter(item ->
		{
			final int id = idFunction.apply(item);
			final String name = nameFunction.apply(item);
			return (Integer.toString(id).equals(value) || isMatching(name, value)) && (displayed.incrementAndGet() < SEARCH_RESULTS) && added.add(id);
		}).collect(Collectors.toList());
	}
	
	public static boolean isMatching(String name, String value)
	{
		name = name.toLowerCase();
		value = value.toLowerCase();
		final boolean startsWith = value.startsWith("*");
		final boolean endsWith = value.endsWith("*");
		final boolean contains = endsWith && startsWith;
		if (endsWith)
		{
			value = value.substring(0, value.length() - 1);
		}
		if (startsWith)
		{
			value = value.substring(1);
		}
		if (contains)
		{
			if (value.contains(name))
			{
				return true;
			}
		}
		return !name.isEmpty() && (name.startsWith(value) || name.endsWith(value));
	}
	
	private AnswerInlineQuery converteResultsToResponse(InlineQuery inlineQuery, List<T> results)
	{
		final AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
		answerInlineQuery.setInlineQueryId(inlineQuery.getId());
		answerInlineQuery.setCacheTime(CACHETIME);
		answerInlineQuery.setResults(convertRaeResults(results));
		return answerInlineQuery;
	}
	
	private List<InlineQueryResult> convertRaeResults(List<T> items)
	{
		List<InlineQueryResult> results = new ArrayList<>();
		
		for (int i = 0; i < items.size(); i++)
		{
			final T item = items.get(i);
			final InputTextMessageContent messageContent = new InputTextMessageContent();
			messageContent.enableMarkdown(true);
			messageContent.setMessageText(escape(parseData(item)));
			
			final InlineQueryResultArticle article = new InlineQueryResultArticle();
			article.setInputMessageContent(messageContent);
			article.setId(Integer.toString(i));
			article.setTitle(escape(parseTitle(item)));
			article.setDescription(escape(parseDescription(item)));
			article.setThumbUrl(parseThumbUrl(item));
			results.add(article);
		}
		
		return results;
	}
	
	protected abstract String parseData(T item);
	
	protected abstract String parseTitle(T item);
	
	protected abstract String parseDescription(T item);
	
	protected abstract String parseThumbUrl(T item);
	
	protected abstract String getHelpMessage();
	
	protected static String escape(String text)
	{
		text = text.replaceAll("&", "&amp;");
		text = text.replaceAll("<", "&lt;");
		text = text.replaceAll(">", "&gt;");
		text = text.replaceAll("\"", "&quot;");
		return text;
	}
	
	private SendMessage getHelpMessage(Message message)
	{
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(message.getChatId().toString());
		sendMessage.enableMarkdown(true);
		sendMessage.setText(getHelpMessage());
		return sendMessage;
	}
	
	@Override
	public final String getBotUsername()
	{
		return _username;
	}
	
	@Override
	public final String getBotToken()
	{
		return _token;
	}
}
