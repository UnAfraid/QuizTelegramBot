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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author UnAfraid
 * @param <T>
 */
@FunctionalInterface
public interface ExConsumer<T>
{
	/**
	 * Performs this operation on the given argument.
	 * @param t the input argument
	 * @throws Exception
	 */
	void accept(T t) throws Exception;
	
	/**
	 * Returns a composed {@code Consumer} that performs, in sequence, this operation followed by the {@code after} operation. If performing either operation throws an exception, it is relayed to the caller of the composed operation. If performing this operation throws an exception, the
	 * {@code after} operation will not be performed.
	 * @param after the operation to perform after this operation
	 * @return a composed {@code Consumer} that performs in sequence this operation followed by the {@code after} operation
	 * @throws Exception
	 * @throws NullPointerException if {@code after} is null
	 */
	default Consumer<T> andThen(Consumer<? super T> after) throws Exception
	{
		Objects.requireNonNull(after);
		AtomicReference<Exception> exceptionRef = new AtomicReference<>();
		Consumer<T> result = (T t) ->
		{
			try
			{
				accept(t);
			}
			catch (Exception e)
			{
				exceptionRef.set(e);
			}
			after.accept(t);
		};
		if (exceptionRef.get() != null)
		{
			throw exceptionRef.get();
		}
		return result;
	}
}
