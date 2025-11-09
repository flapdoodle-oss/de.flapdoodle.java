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
package de.flapdoodle.collections;

import de.flapdoodle.checks.Preconditions;
import org.immutables.value.Value;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value.Immutable
public abstract class GroupBy<S, T, K> implements Function<List<S>, List<T>> {
	protected abstract Function<S, T> map();

	protected abstract Function<T, K> classifier();

	protected abstract BiFunction<T, List<S>, T> merge();

	@Override
	@Value.Auxiliary
	public List<T> apply(List<S> sources) {
		return groupBy(sources, map(), classifier(), merge());
	}

	public static <T> WithSourceType<T> withListOf(Class<T> sourceType) {
		return new WithSourceType<T>(sourceType);
	}

	public static class WithSourceType<S> {
		private final Class<S> sourceType;
		public WithSourceType(Class<S> sourceType) {
			this.sourceType = sourceType;
		}

		public <T> MapSourceType<S, T> map(Function<S, T> map) {
			return new MapSourceType<>(sourceType, map);
		}
	}

	public static class MapSourceType<S, T> {

		private final Class<S> sourceType;
		private final Function<S, T> map;
		public MapSourceType(Class<S> sourceType, Function<S, T> map) {
			this.sourceType = sourceType;
			this.map = map;
		}

		public <K> WithClassifier<S, T, K> indetifiedBy(Function<T, K> classifier) {
			return new WithClassifier<>(sourceType, map, classifier);
		}
	}

	public static class WithClassifier<S, T, K> {

		private final Class<S> sourceType;
		private final Function<S, T> map;
		private final Function<T, K> classifier;
		public WithClassifier(Class<S> sourceType, Function<S, T> map, Function<T, K> classifier) {
			this.sourceType = sourceType;
			this.map = map;
			this.classifier = classifier;
		}

		public GroupBy<S, T, K> merge(BiFunction<T, List<S>, T> merge) {
			return ImmutableGroupBy.<S, T, K>builder()
				.map(map)
				.classifier(classifier)
				.merge(merge)
				.build();
		}

		public <M> GroupBy<S, T, K> merge(BiFunction<T, List<M>, T> merge, Function<S, M> mapMerge) {
			return ImmutableGroupBy.<S, T, K>builder()
				.map(map)
				.classifier(classifier)
				.merge((entry, list) -> merge.apply(entry, list.stream().map(mapMerge).collect(Collectors.toList())))
				.build();
		}
	}


	private static <S, T, K> List<T> groupBy(
		List<S> sources,
		Function<S, T> mapFunction,
		Function<T, K> keyFunction,
		BiFunction<T, List<S>, T> groupFunction
	) {
		LinkedHashMap<T, List<S>> map = sources.stream()
			.collect(Collectors.groupingBy(mapFunction, LinkedHashMap::new, Collectors.mapping(s -> s, Collectors.toList())));

		List<String> keyCollisions = map.keySet().stream()
			.collect(Collectors.groupingBy(keyFunction, LinkedHashMap::new, Collectors.toList()))
			.entrySet().stream()
			.filter(it -> it.getValue().size() > 1)
			.map(it -> "same key '" + it.getKey() + "' from " + it.getValue())
			.collect(Collectors.toList());

		Preconditions.checkArgument(keyCollisions.isEmpty(), "key collisions: %s", keyCollisions);

		return map.entrySet().stream()
			.map(entry -> groupFunction.apply(entry.getKey(), entry.getValue()))
			.collect(Collectors.toList());
	}
}
