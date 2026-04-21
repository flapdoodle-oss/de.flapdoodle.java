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
package de.flapdoodle.hash;

import de.flapdoodle.types.Try;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.function.Function;

public class Hasher {

	private final MessageDigest digest;

	private Hasher(MessageDigest digest) {
		this.digest = digest;
	}

	public Hasher update(String content, Charset charset) {
		digest.update(content.getBytes(charset));
		return this;
	}

	public Hasher update(String content) {
		return update(content, StandardCharsets.UTF_8);
	}

	public Hasher update(byte[] content) {
		digest.update(content);
		return this;
	}

	public Hasher update(ByteBuffer content) {
		digest.update(content);
		return this;
	}

	public String hashAsString() {
		return map(Hasher::byteArrayToHex);
	}

	public String hashAsUrl() {
		return map(src -> new String(Base64.getUrlEncoder()
			.withoutPadding()
			.encode(src), StandardCharsets.UTF_8));
	}

	public <T> T map(Function<byte[], T> mapper) {
		byte[] result = digest.digest();
		return mapper.apply(result);
	}

	public static Hasher instance() {
		return new Hasher(Try.get(() -> MessageDigest.getInstance("SHA-256")));
	}

	public static Hasher md5Instance() {
		return new Hasher(Try.get(() -> MessageDigest.getInstance("MD5")));
	}

	private static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte b : a) sb.append(String.format("%02x", b));
		return sb.toString();
	}
}
