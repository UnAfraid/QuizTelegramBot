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
package com.github.unafraid.telegram.quizbot.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.l2junity.commons.util.IXmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.github.unafraid.telegram.quizbot.util.StatsSet;

/**
 * @author UnAfraid
 */
public class QuizData implements IXmlReader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(HandlersData.class);
	private final List<QuizQuestion> _quizQuestions = new ArrayList<>();
	
	protected QuizData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_quizQuestions.clear();
		File quizFile = new File("config/Quiz-Questions.xml");
		if (!quizFile.exists())
		{
			quizFile = new File("dist/config/Quiz-Questions.xml");
		}
		parseFile(quizFile);
		LOGGER.info("Loaded: {} quiz questions", _quizQuestions.size());
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "question", questionNode ->
		{
			final QuizQuestion question = new QuizQuestion(new StatsSet(parseAttributes(questionNode)));
			forEach(questionNode, "answer", innerNode ->
			{
				final StatsSet set = new StatsSet(parseAttributes(innerNode));
				set.set(".text", innerNode.getTextContent());
				question.addAnswer(new QuizAnswer(set));
			});
			_quizQuestions.add(question);
		}));
	}
	
	public List<QuizQuestion> getQuizQuestions()
	{
		return _quizQuestions;
	}
	
	public static class QuizQuestion
	{
		private final String _question;
		private final String _file;
		private final int _answersPerRow;
		private final int _maxAnswersPerPerson;
		private final int _maxIncorrectAnswers;
		private final List<QuizAnswer> _answers = new ArrayList<>();
		
		public QuizQuestion(StatsSet set)
		{
			_question = set.getString("text", null);
			_file = set.getString("file", null);
			_answersPerRow = set.getInt("answersPerRow", 3);
			_maxAnswersPerPerson = set.getInt("maxAnswersPerPerson", 1);
			_maxIncorrectAnswers = set.getInt("maxIncorrectAnswers", Integer.MAX_VALUE);
		}
		
		public String getQuestion()
		{
			return _question;
		}
		
		public String getFile()
		{
			return _file;
		}
		
		public int getAnswersPerRow()
		{
			return _answersPerRow;
		}
		
		public int getMaxAnswersPerPerson()
		{
			return _maxAnswersPerPerson;
		}
		
		public int getMaxIncorrectAnswers()
		{
			return _maxIncorrectAnswers;
		}
		
		public List<QuizAnswer> getAnswers()
		{
			return _answers;
		}
		
		public void addAnswer(QuizAnswer answer)
		{
			_answers.add(answer);
		}
		
		public int getCorrectAnswersCount()
		{
			return (int) _answers.stream().filter(QuizAnswer::isCorrect).count();
		}
	}
	
	public static class QuizAnswer
	{
		private final String _answer;
		private final boolean _correct;
		
		public QuizAnswer(StatsSet set)
		{
			_answer = set.getString(".text");
			_correct = set.getBoolean("correct", false);
		}
		
		public String getAnswer()
		{
			return _answer;
		}
		
		public boolean isCorrect()
		{
			return _correct;
		}
		
		public boolean isIncorrect()
		{
			return !_correct;
		}
	}
	
	public static QuizData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final QuizData INSTANCE = new QuizData();
	}
}
