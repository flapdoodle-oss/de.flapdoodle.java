/*
 * Copyright (C) 2016
 *   Michael Mosmann <michael@mosmann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.types;

import java.util.function.Function;

public sealed interface Maybe<T> permits Maybe.None, Maybe.Some {
	boolean hasSome();
	@Nullable
	T get();
	<R> Maybe<R> map(Function<T, R> mapping);

	default T getOrThrow(IllegalArgumentException e) {
		if (!hasSome()) throw e;
		return get();
	}

	record Some<T>(@Nullable T value) implements Maybe<T> {

		@Override
		public boolean hasSome() {
			return true;
		}

		@Override
		public T get() {
			return value();
		}

		@Override
		public <R> Maybe<R> map(Function<T, R> mapping) {
			return Maybe.some(mapping.apply(value()));
		}
	}

	record None<T>() implements Maybe<T> {

		@Override
		public boolean hasSome() {
			return false;
		}

		@Override
		public T get() {
			throw new IllegalArgumentException("is none");
		}

		@Override
		public <R> Maybe<R> map(Function<T, R> mapping) {
			return none();
		}
	}

	static <T> None<T> none() {
		return new None<>();
	}

	static <T> Some<T> some(@Nullable T value) {
		return new Some<>(value);
	}
}
