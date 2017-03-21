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
package com.github.unafraid.telegram.quizbot.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.github.unafraid.telegram.quizbot.bothandlers.ChannelBot;
import com.github.unafraid.telegram.quizbot.data.QuizData;
import com.github.unafraid.telegram.quizbot.data.QuizData.QuizAnswer;
import com.github.unafraid.telegram.quizbot.data.QuizData.QuizQuestion;
import com.github.unafraid.telegram.quizbot.util.BotUtil;

/**
 * @author UnAfraid
 */
public class QuizHandler implements IMessageHandler
{
	private final List<QuizActiveQuestion> _answeredQuestions = new ArrayList<>();
	private final Queue<QuizQuestion> _pendingQuestions = new LinkedBlockingQueue<>();
	private QuizActiveQuestion _activeQuestion = null;
	private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();
	
	public void startQuiz(ChannelBot bot, Message message) throws TelegramApiException
	{
		final QuizActiveQuestion activeQuestion = _activeQuestion;
		if (activeQuestion != null)
		{
			BotUtil.sendMessage(bot, message, "There's already another quiz running!", true, true, null);
			return;
		}
		
		_lock.writeLock().lock();
		
		try
		{
			_answeredQuestions.clear();
			_pendingQuestions.addAll(QuizData.getInstance().getQuizQuestions());
			final QuizQuestion nextQuestion = _pendingQuestions.poll();
			if (nextQuestion != null)
			{
				_activeQuestion = new QuizActiveQuestion(nextQuestion);
				onQuestionAsked(nextQuestion, bot, message);
			}
		}
		finally
		{
			_lock.writeLock().unlock();
		}
	}
	
	public void stopQuiz(ChannelBot bot, Message message) throws TelegramApiException
	{
		final QuizActiveQuestion activeQuestion = _activeQuestion;
		if (activeQuestion == null)
		{
			BotUtil.sendMessage(bot, message, "There isn't any quiz running right now!", true, true, null);
			return;
		}
		
		_lock.writeLock().lock();
		
		try
		{
			_pendingQuestions.clear();
			_activeQuestion = null;
			BotUtil.sendMessage(bot, message, "Quiz has been canceled", true, true, null);
		}
		finally
		{
			_lock.writeLock().unlock();
		}
	}
	
	public void quizSkip(ChannelBot bot, Message message) throws TelegramApiException
	{
		final QuizActiveQuestion activeQuestion = _activeQuestion;
		if (activeQuestion == null)
		{
			BotUtil.sendMessage(bot, message, "There isn't any quiz running right now!", true, true, new ReplyKeyboardRemove());
			return;
		}
		
		_lock.writeLock().lock();
		
		try
		{
			BotUtil.sendMessage(bot, message, "Skipping current question", true, true, new ReplyKeyboardRemove());
			_answeredQuestions.add(activeQuestion);
			final QuizQuestion nextQuestion = _pendingQuestions.poll();
			if (nextQuestion != null)
			{
				_activeQuestion = new QuizActiveQuestion(nextQuestion);
				onQuestionAsked(nextQuestion, bot, message);
			}
		}
		finally
		{
			_lock.writeLock().unlock();
		}
	}
	
	public void quizReport(ChannelBot bot, Message message) throws TelegramApiException
	{
		final StringJoiner sj = new StringJoiner(System.lineSeparator());
		sj.add("Questions answered: " + _answeredQuestions.size());
		for (QuizActiveQuestion activeQuestion : _answeredQuestions)
		{
			final boolean maxIncorrectReached = activeQuestion.getIncorrectAnswersCount() >= activeQuestion.getQuestion().getMaxIncorrectAnswers();
			final boolean maxCorrectReached = activeQuestion.getCorrectAnswersCount() >= activeQuestion.getQuestion().getCorrectAnswersCount();
			final String question = activeQuestion.getQuestion().getQuestion();
			final String file = activeQuestion.getQuestion().getFile();
			if (question != null)
			{
				sj.add("Question: *\"" + question + "\"*");
			}
			if (file != null)
			{
				sj.add("File: *\"" + file + "\"*");
			}
			sj.add("   | Answered: " + (maxCorrectReached ? "✅" : maxIncorrectReached ? "🅾" : "🖍"));
			for (QuizParticipant participant : activeQuestion.getParticipants().values())
			{
				final StringBuilder sb = new StringBuilder();
				for (QuizAnswer answer : participant.getAnswers())
				{
					sb.append(" *\"").append(answer.getAnswer()).append("\"* correct: ").append(answer.isCorrect() ? "✅" : "🅾").append(",");
				}
				final String answers = sb.substring(0, sb.length() - 1);
				sj.add("   | Participant: @" + participant.getUsername() + " answered: " + answers);
			}
			sj.add("-------------------------------------------------------------------------------------------------");
		}
		
		BotUtil.sendMessage(bot, message, sj.toString(), true, true, null);
	}
	
	@Override
	public boolean onMessage(ChannelBot bot, Message message) throws TelegramApiException
	{
		// We're Interested in reply only
		if (!message.isReply())
		{
			return false;
		}
		
		final QuizActiveQuestion activeQuestion = getActiveQuestion();
		if (activeQuestion != null)
		{
			for (QuizAnswer answer : activeQuestion.getQuestion().getAnswers())
			{
				// Verify if the text participant inputed is an answer
				if (answer.getAnswer().equalsIgnoreCase(message.getText()))
				{
					// Verify if participant already 'participated'
					final QuizParticipant participant = activeQuestion.getParticipant(message.getFrom().getId());
					if ((participant == null) || (participant.getAnswers().size() < activeQuestion.getQuestion().getMaxAnswersPerPerson()))
					{
						onQuestionAnswered(activeQuestion, answer, bot, message);
						return true;
					}
					return false;
				}
			}
		}
		return false;
	}
	
	private void onQuestionAsked(QuizQuestion nextQuestion, ChannelBot bot, Message message) throws TelegramApiException
	{
		final StringJoiner sj = new StringJoiner(System.lineSeparator());
		final String question = nextQuestion.getQuestion();
		final String file = nextQuestion.getFile();
		if (question != null)
		{
			sj.add("The question is: " + nextQuestion.getQuestion());
		}
		else
		{
			BotUtil.sendPhoto(bot, message, null, false, new File(file));
		}
		if (nextQuestion.getMaxIncorrectAnswers() < Integer.MAX_VALUE)
		{
			sj.add("You have " + nextQuestion.getMaxIncorrectAnswers() + " maximum incorrect answer attempts");
		}
		sj.add("There are " + nextQuestion.getCorrectAnswersCount() + " correct answers");
		
		final ReplyKeyboardMarkup markup = getKeyboardMarkup(nextQuestion);
		
		BotUtil.sendMessage(bot, message, sj.toString(), false, true, markup);
	}
	
	private ReplyKeyboardMarkup getKeyboardMarkup(QuizQuestion nextQuestion)
	{
		final ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
		markup.setOneTimeKeyboad(true);
		for (QuizAnswer answer : nextQuestion.getAnswers())
		{
			final KeyboardRow row;
			if (markup.getKeyboard().isEmpty() || (markup.getKeyboard().get(markup.getKeyboard().size() - 1).size() >= nextQuestion.getAnswersPerRow()))
			{
				row = new KeyboardRow();
				markup.getKeyboard().add(row);
			}
			else
			{
				row = markup.getKeyboard().get(markup.getKeyboard().size() - 1);
			}
			row.add(new KeyboardButton(answer.getAnswer()));
		}
		return markup;
	}
	
	private void onQuestionAnswered(QuizActiveQuestion activeQuestion, QuizAnswer answer, ChannelBot bot, Message message) throws TelegramApiException
	{
		final User from = message.getFrom();
		
		// Register participants's answer
		QuizParticipant participant = activeQuestion.getOrCreateParticipant(from.getId(), from.getFirstName(), from.getUserName());
		participant.addAnswer(answer);
		
		// Announce if participant's answer is correct or incorrect
		BotUtil.sendMessage(bot, message, "The following answer: " + message.getText() + " is " + (answer.isCorrect() ? "✅ Correct!" : "🅾 Incorrect!"), true, true, getKeyboardMarkup(activeQuestion.getQuestion()));
		
		// Verify if all necessary answers were provided and proceed to next question
		final boolean maxIncorrectReached = activeQuestion.getIncorrectAnswersCount() >= activeQuestion.getQuestion().getMaxIncorrectAnswers();
		final boolean maxCorrectReached = activeQuestion.getCorrectAnswersCount() >= activeQuestion.getQuestion().getCorrectAnswersCount();
		
		if (maxIncorrectReached || maxCorrectReached)
		{
			// Mark the question as answered
			_answeredQuestions.add(activeQuestion);
			
			// Proceed to the next one
			final QuizQuestion nextQuestion = _pendingQuestions.poll();
			if (nextQuestion != null)
			{
				BotUtil.sendMessage(bot, message, maxIncorrectReached ? "Maximum incorrect answers reached failed to answer this one, proceeding to next question!" : "All necessary answers were provided, proceeding to next question!", false, true, new ReplyKeyboardRemove());
				setQuestion(nextQuestion);
				onQuestionAsked(nextQuestion, bot, message);
			}
			else
			{
				_lock.writeLock().lock();
				
				try
				{
					_pendingQuestions.clear();
					_activeQuestion = null;
					BotUtil.sendMessage(bot, message, "All questions have been answered!", false, true, new ReplyKeyboardRemove());
				}
				finally
				{
					_lock.writeLock().unlock();
				}
			}
		}
	}
	
	private void setQuestion(QuizQuestion question)
	{
		_lock.writeLock().lock();
		try
		{
			_activeQuestion = new QuizActiveQuestion(question);
		}
		finally
		{
			_lock.writeLock().unlock();
		}
	}
	
	private QuizActiveQuestion getActiveQuestion()
	{
		_lock.readLock().lock();
		try
		{
			return _activeQuestion;
		}
		finally
		{
			_lock.readLock().unlock();
		}
	}
	
	static class QuizActiveQuestion
	{
		private final QuizQuestion _question;
		private final Map<Integer, QuizParticipant> _participants = new ConcurrentHashMap<>();
		
		public QuizActiveQuestion(QuizQuestion question)
		{
			_question = question;
		}
		
		public QuizQuestion getQuestion()
		{
			return _question;
		}
		
		public Map<Integer, QuizParticipant> getParticipants()
		{
			return _participants;
		}
		
		public QuizParticipant getOrCreateParticipant(int id, String name, String username)
		{
			return _participants.computeIfAbsent(id, key -> new QuizParticipant(id, name, username));
		}
		
		public QuizParticipant getParticipant(int id)
		{
			return _participants.get(id);
		}
		
		public int getCorrectAnswersCount()
		{
			return (int) _participants.values().stream().flatMap(QuizParticipant::getAnswersStream).distinct().filter(QuizAnswer::isCorrect).count();
		}
		
		public int getIncorrectAnswersCount()
		{
			return (int) _participants.values().stream().flatMap(QuizParticipant::getAnswersStream).distinct().filter(QuizAnswer::isIncorrect).count();
		}
	}
	
	static class QuizParticipant
	{
		private final int _id;
		private final String _name;
		private final String _username;
		private final Set<QuizAnswer> _answers = new LinkedHashSet<>();
		
		public QuizParticipant(int id, String name, String username)
		{
			_id = id;
			_name = name;
			_username = username;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public String getUsername()
		{
			return _username;
		}
		
		public Set<QuizAnswer> getAnswers()
		{
			return _answers;
		}
		
		public Stream<QuizAnswer> getAnswersStream()
		{
			return _answers.stream();
		}
		
		public void addAnswer(QuizAnswer answer)
		{
			_answers.add(answer);
		}
		
		public boolean hasCorrectAnswer()
		{
			return _answers.stream().anyMatch(QuizAnswer::isCorrect);
		}
		
		public boolean hasIncorrectAnswer()
		{
			return !_answers.stream().anyMatch(QuizAnswer::isCorrect);
		}
	}
	
	public static QuizHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final QuizHandler INSTANCE = new QuizHandler();
	}
}
