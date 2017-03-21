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
package com.github.unafraid.telegram.quizbot;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;

import com.github.unafraid.telegram.quizbot.data.HandlersData;
import com.github.unafraid.telegram.quizbot.data.QuizData;
import com.github.unafraid.telegram.quizbot.handlers.CommandHandler;

import sun.management.VMManagement;

/**
 * @author UnAfraid
 */
public final class Main extends Thread
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args)
	{
		writePID();
		
		BotConfig.load();
		ApiContextInitializer.init();
		CommandHandler.getInstance();
		HandlersData.getInstance();
		QuizData.getInstance();
		BotManager.getInstance();
		ShutdownThread.init();
	}
	
	private static final void writePID()
	{
		final File pid = new File("telegram-bot.pid");
		try
		{
			Files.write(pid.toPath(), String.valueOf(getPID()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		}
		catch (Exception e)
		{
			LOGGER.warn("Couldn't write PID", e);
		}
	}
	
	private static int getPID()
	{
		try
		{
			final RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
			final Field jvmField = runtime.getClass().getDeclaredField("jvm");
			jvmField.setAccessible(true);
			
			final VMManagement mgmt = (VMManagement) jvmField.get(runtime);
			final Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
			pid_method.setAccessible(true);
			
			return (Integer) pid_method.invoke(mgmt);
		}
		catch (Exception e)
		{
			LOGGER.warn("Couldn't find PID", e);
		}
		return -1;
	}
}
