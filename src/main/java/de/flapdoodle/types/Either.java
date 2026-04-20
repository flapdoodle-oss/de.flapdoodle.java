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

import java.util.NoSuchElementException;
import java.util.function.Function;

public sealed interface Either<L, R> permits Either.Left, Either.Right {

	boolean isLeft();

	@Nullable
	L left();

	@Nullable
	R right();

	default <T> Either<T, R> mapLeft(Function<L, T> transformation) {
		return isLeft()
			? left(transformation.apply(left()))
			: (Either<T, R>) this;
	}

	default <T> Either<L, T> mapRight(Function<R, T> transformation) {
		return isLeft()
			? (Either<L, T>) this
			: right(transformation.apply(right()));
	}

	default <T> T map(Function<L, T> leftTransformation, Function<R, T> rightTransformation) {
		Either<T, T> mapped = mapLeft(leftTransformation).mapRight(rightTransformation);
		return mapped.isLeft() ? mapped.left() : mapped.right();
	}

	record Left<L, R>(@Nullable L left) implements Either<L, R> {

		@Override
		public R right() {
			throw new NoSuchElementException("is left");
		}
		@Override
		public boolean isLeft() {
			return true;
		}
	}

	record Right<L, R>(@Nullable R right) implements Either<L, R> {

		@Override
		public L left() {
			throw new NoSuchElementException("is right");
		}
		@Override
		public boolean isLeft() {
			return false;
		}
	}


	static <L, R> Either<L, R> left(L left) {
		return new Left<>(left);
	}

	static <L, R> Either<L, R> right(R right) {
		return new Right<>(right);
	}
}
