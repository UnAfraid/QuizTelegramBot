/*
 * Copyright 2010 InC-Gaming, Patrick Biesenbach aka. Forsaiken. All rights reserved.
 */
package com.github.unafraid.telegram.quizbot.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.unafraid.telegram.quizbot.Main;

/**
 * @author Forsaiken
 */
public final class ThreadPoolManager
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	private static ThreadPoolManager _instance;
	
	public static final void init()
	{
		_instance = new ThreadPoolManager();
	}
	
	public static final ThreadPoolManager getInstance()
	{
		return _instance;
	}
	
	private final IncScheduledThreadPoolExecutor _scheduledThreadPool;
	private final IncThreadPoolExecutor _threadPool;
	
	private ThreadPoolManager()
	{
		_scheduledThreadPool = new IncScheduledThreadPoolExecutor(4, "STP");
		_threadPool = new IncThreadPoolExecutor(2, 4, "TP");
	}
	
	public final ScheduledFuture<?> schedule(final Runnable command, final long delay)
	{
		return _scheduledThreadPool.schedule(command, delay);
	}
	
	public final ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initial, final long delay)
	{
		return _scheduledThreadPool.scheduleAtFixedRate(command, initial, delay);
	}
	
	public final void execute(final Runnable r)
	{
		_threadPool.execute(new IncScheduledRunnableWrapper(r));
	}
	
	public final void shutdown()
	{
		LOGGER.info("Shuting down all thread pools...");
		shutdown(_scheduledThreadPool);
		shutdown(_threadPool);
		LOGGER.info("Shutdown complete.");
	}
	
	public final void purge()
	{
		_scheduledThreadPool.purge();
		_threadPool.purge();
	}
	
	private static final class IncScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor
	{
		public IncScheduledThreadPoolExecutor(final int corePoolSize, final String threadGroupName)
		{
			super(corePoolSize, new IncPriorityThreadFactory(threadGroupName, Thread.NORM_PRIORITY), IncRejectedExecutionHandler.STATIC_INSTANCE);
		}
		
		public final ScheduledFuture<?> schedule(final Runnable command, final long delay)
		{
			return super.schedule(new IncScheduledRunnableWrapper(command), delay, TimeUnit.MILLISECONDS);
		}
		
		public final ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initial, final long delay)
		{
			return super.scheduleAtFixedRate(new IncScheduledRunnableWrapper(command), initial, delay, TimeUnit.MILLISECONDS);
		}
	}
	
	private static final class IncThreadPoolExecutor extends ThreadPoolExecutor
	{
		public IncThreadPoolExecutor(final int corePoolSize, final int maxCorePoolSize, final String threadGroupName)
		{
			this(corePoolSize, maxCorePoolSize, threadGroupName, Thread.NORM_PRIORITY);
		}
		
		public IncThreadPoolExecutor(final int corePoolSize, final int maxCorePoolSize, final String threadGroupName, final int threadPriority)
		{
			super(corePoolSize, maxCorePoolSize, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new IncPriorityThreadFactory(threadGroupName, threadPriority), IncRejectedExecutionHandler.STATIC_INSTANCE);
		}
	}
	
	private static final void shutdown(final ThreadPoolExecutor tpe)
	{
		if (!(tpe instanceof IncThreadPoolExecutor) && !(tpe instanceof IncScheduledThreadPoolExecutor))
		{
			throw new IllegalArgumentException();
		}
		
		try
		{
			tpe.awaitTermination(1, TimeUnit.SECONDS);
		}
		catch (final InterruptedException e)
		{
		
		}
		finally
		{
			tpe.shutdown();
		}
	}
	
	private static final class IncRejectedExecutionHandler implements RejectedExecutionHandler
	{
		public static IncRejectedExecutionHandler STATIC_INSTANCE = new IncRejectedExecutionHandler();
		
		@Override
		public final void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor)
		{
		
		}
	}
	
	private static final class IncPriorityThreadFactory implements ThreadFactory
	{
		private final int _threadPriority;
		private final String _threadGroupName;
		private final ThreadGroup _threadGroup;
		private final AtomicInteger _threadId;
		
		public IncPriorityThreadFactory(final String threadGroupName, final int threadPriority)
		{
			_threadGroupName = threadGroupName;
			_threadPriority = threadPriority;
			_threadGroup = new ThreadGroup(_threadGroupName);
			_threadId = new AtomicInteger();
		}
		
		@Override
		public final Thread newThread(final Runnable r)
		{
			final Thread t = new Thread(_threadGroup, r, _threadGroupName + '[' + _threadId.getAndIncrement() + ']');
			t.setPriority(_threadPriority);
			return t;
		}
	}
	
	private static final class IncScheduledRunnableWrapper implements Runnable
	{
		public final Runnable _r;
		
		public IncScheduledRunnableWrapper(final Runnable r)
		{
			_r = r;
		}
		
		@Override
		public final void run()
		{
			try
			{
				_r.run();
			}
			catch (final Throwable e)
			{
				final Thread t = Thread.currentThread();
				t.getUncaughtExceptionHandler().uncaughtException(t, e);
			}
		}
	}
	
	/**
	 * @return
	 */
	public String[] getStats()
	{
		return new String[]
		{
			"STP:",
			" + Effects:",
			" |- ActiveThreads:   " + _scheduledThreadPool.getActiveCount(),
			" |- getCorePoolSize: " + _scheduledThreadPool.getCorePoolSize(),
			" |- PoolSize:        " + _scheduledThreadPool.getPoolSize(),
			" |- MaximumPoolSize: " + _scheduledThreadPool.getMaximumPoolSize(),
			" |- CompletedTasks:  " + _scheduledThreadPool.getCompletedTaskCount(),
			" |- ScheduledTasks:  " + _scheduledThreadPool.getQueue().size(),
			" | -------",
			"TP:",
			" + Packets:",
			" |- ActiveThreads:   " + _threadPool.getActiveCount(),
			" |- getCorePoolSize: " + _threadPool.getCorePoolSize(),
			" |- MaximumPoolSize: " + _threadPool.getMaximumPoolSize(),
			" |- LargestPoolSize: " + _threadPool.getLargestPoolSize(),
			" |- PoolSize:        " + _threadPool.getPoolSize(),
			" |- CompletedTasks:  " + _threadPool.getCompletedTaskCount(),
			" |- QueuedTasks:     " + _threadPool.getQueue().size(),
			" | -------",
		};
	}
}
