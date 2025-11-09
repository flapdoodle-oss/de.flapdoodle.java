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

import org.immutables.value.Value;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GroupByTest {

	@Test
	void groupTableByPersonAdressAndReachableHours() {
		List<Source> sources = Arrays.asList(
			source("Peter", 33, "daheim", "Lübeck", "Hauptstraße", 7),
			source("Peter", 33, "daheim", "Lübeck", "Hauptstraße", 8),
			source("Peter", 33, "daheim", "Lübeck", "Hauptstraße", 9),
			source("Peter", 33, "arbeit", "Lübeck", "Stadtring", 10),
			source("Peter", 33, "arbeit", "Lübeck", "Stadtring", 11),
			source("Susi", 29, "daheim", "Mannheim", "Nebenstraße", 8),
			source("Susi", 29, "daheim", "Mannheim", "Nebenstraße", 9),
			source("Susi", 29, "arbeit", "Heidelberg", "Nebenstraße", 8)
		);

		GroupBy<Source, Address, String> groupAddress = GroupBy.withListOf(Source.class)
			.map(it -> address(it.addressLabel(), it.city(), it.street(), listOf()))
			.indetifiedBy(Address::label)
			.merge((address, hours) -> address(address.label(), address.city(), address.street(), hours), Source::reachableAt);

		GroupBy<Source, Person, String> groupPerson = GroupBy.withListOf(Source.class)
			.map(it -> person(it.name(), it.age(), listOf()))
			.indetifiedBy(Person::name)
			.merge((person, list) -> person(person.name(), person.age(), groupAddress.apply(list)));

		List<Person> grouped = groupPerson.apply(sources);

		assertThat(grouped)
			.hasSize(2)
			.containsExactly(person("Peter", 33, listOf(
					address("daheim", "Lübeck", "Hauptstraße", listOf(
						7, 8, 9
					)),
					address("arbeit", "Lübeck", "Stadtring", listOf(
						10, 11
					))
				)),
				person("Susi", 29, listOf(
					address("daheim", "Mannheim", "Nebenstraße", listOf(
						8,9
					)),
					address("arbeit", "Heidelberg", "Nebenstraße", listOf(
						8
					))
				)));
	}

	@Value.Immutable
	static interface Source {
		String name();

		int age();

		String addressLabel();

		String city();

		String street();

		int reachableAt();
	}

	@Value.Immutable
	static interface Person {
		String name();

		int age();

		List<Address> addresses();
	}

	@Value.Immutable
	static interface Address {
		String label();

		String city();

		String street();

		List<Integer> reachableAt();
	}

	private static Address address(String label, String city, String street, List<Integer> reachableAt) {
		return ImmutableAddress.builder().label(label).city(city).street(street).reachableAt(reachableAt).build();
	}

	private static Person person(String name, int age, List<Address> addresses) {
		return ImmutablePerson.builder().name(name).age(age).addresses(addresses).build();
	}

	private static Source source(String name, int age, String addressLabel, String city, String street, int reachableAt) {
		return ImmutableSource.builder()
			.name(name).age(age).addressLabel(addressLabel).city(city).street(street).reachableAt(reachableAt)
			.build();
	}

	private static <T> List<T> listOf(T... values) {
		return Arrays.asList(values);
	}
}