package com.ducbrick.real_time_messaging_api.testutils;

import java.util.Random;

public class Generator {
	private final static Random random = new Random();

	public static String generateRandomString(int length) {
		return random
				.ints('a', 'z' + 1)
				.limit(length)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}

	public static String generateRandomEmail(int nameLength) {
		return generateRandomString(nameLength) + "@gmail.com";
	}
}
