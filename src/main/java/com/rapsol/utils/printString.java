package com.rapsol.utils;

import org.jetbrains.annotations.NotNull;

public class printString implements CharSequence {
	private final String value;

	public printString(String s) {
		this.value = s;
	}

	public printString(String string, String key) {
		this.value = string;
	}

	public static printString of(String s) {
		return new printString(s);
	}

	public static printString of(String string, String key) {
		return new printString(string, key);
	}

	@Override
	public int length() {
		return value.length();
	}

	@Override
	public char charAt(int index) {
		return value.charAt(index);
	}

	@Override
	public @NotNull String toString() {
		return value;
	}

	@Override
	public @NotNull CharSequence subSequence(int start, int end) {
		return value.subSequence(start, end);
	}
}