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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardHide;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import com.github.unafraid.telegram.quizbot.bothandlers.ChannelBot;
import com.github.unafraid.telegram.quizbot.data.QuizData;
import com.github.unafraid.telegram.quizbot.data.QuizData.QuizAnswer;
import com.github.unafraid.telegram.quizbot.data.QuizData.QuizQuestion;
import com.github.unafraid.telegram.quizbot.handlers.commands.IMessageHandler;
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
			BotUtil.sendMessage(bot, message, "There isn't any quiz running right now!", true, true, new ReplyKeyboardHide());
			return;
		}
		
		_lock.writeLock().lock();
		
		try
		{
			BotUtil.sendMessage(bot, message, "Skipping current question", true, true, new ReplyKeyboardHide());
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
			
			sj.add("Question: " + activeQuestion.getQuestion().getQuestion());
			sj.add("   | Answered: " + (maxCorrectReached ? "✅" : maxIncorrectReached ? "🅾" : "🖍"));
			for (QuizParticipant participant : activeQuestion.getParticipants())
			{
				sj.add("   | Participant: @" + participant.getUsername() + " answered correctly: " + (participant.hasCorrectAnswer() ? "✅" : "🅾"));
			}
			sj.add("-------------------------------------------");
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
					if (activeQuestion.hasParticipated(message.getFrom().getId()))
					{
						return false;
					}
					
					onQuestionAnswered(activeQuestion, answer, bot, message);
					return true;
				}
			}
		}
		return false;
	}
	
	private void onQuestionAsked(QuizQuestion nextQuestion, ChannelBot bot, Message message) throws TelegramApiException
	{
		final StringJoiner sj = new StringJoiner(System.lineSeparator());
		sj.add("The question is: " + nextQuestion.getQuestion());
		if (nextQuestion.getMaxIncorrectAnswers() < Integer.MAX_VALUE)
		{
			sj.add("You have " + nextQuestion.getMaxIncorrectAnswers() + " maximum incorrect answer attempts");
		}
		sj.add("There are " + nextQuestion.getCorrectAnswersCount() + " correct answers");
		
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
		
		BotUtil.sendMessage(bot, message, sj.toString(), false, true, markup);
	}
	
	private void onQuestionAnswered(QuizActiveQuestion activeQuestion, QuizAnswer answer, ChannelBot bot, Message message) throws TelegramApiException
	{
		final User from = message.getFrom();
		
		// Register participants's answer
		activeQuestion.addParticipant(new QuizParticipant(from.getId(), from.getFirstName(), from.getUserName(), answer.isCorrect()));
		
		// Announce if participant's answer is correct or incorrect
		BotUtil.sendMessage(bot, message, "The following answer: " + message.getText() + " is " + (answer.isCorrect() ? "✅ Correct!" : "🅾 Incorrect!"), true, true, null);
		
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
				BotUtil.sendMessage(bot, message, maxIncorrectReached ? "Maximum incorrect answers reached failed to answer this one, proceeding to next question!" : "All necessary answers were provided, proceeding to next question!", false, true, new ReplyKeyboardHide());
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
					BotUtil.sendMessage(bot, message, "All questions have been answered!", false, true, new ReplyKeyboardHide());
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
		private final List<QuizParticipant> _participants = new ArrayList<>();
		
		public QuizActiveQuestion(QuizQuestion question)
		{
			_question = question;
		}
		
		public QuizQuestion getQuestion()
		{
			return _question;
		}
		
		public List<QuizParticipant> getParticipants()
		{
			return _participants;
		}
		
		public void addParticipant(QuizParticipant participant)
		{
			_participants.add(participant);
		}
		
		public boolean hasParticipated(int userId)
		{
			return _participants.stream().anyMatch(participant -> participant.getId() == userId);
		}
		
		public int getCorrectAnswersCount()
		{
			return (int) _participants.stream().filter(QuizParticipant::hasCorrectAnswer).count();
		}
		
		public int getIncorrectAnswersCount()
		{
			return (int) _participants.stream().filter(QuizParticipant::hasIncorrectAnswer).count();
		}
	}
	
	static class QuizParticipant
	{
		private final int _id;
		private final String _name;
		private final String _username;
		private final boolean _correctAnswer;
		
		public QuizParticipant(int id, String name, String username, boolean correctAnswer)
		{
			_id = id;
			_name = name;
			_username = username;
			_correctAnswer = correctAnswer;
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
		
		public boolean hasCorrectAnswer()
		{
			return _correctAnswer;
		}
		
		public boolean hasIncorrectAnswer()
		{
			return !_correctAnswer;
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
