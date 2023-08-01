package com.wzy.thirdParty.common;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

public class GithubLoginStateGenerator {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 32;

    private final Random random;
    private final char[] buffer;

    public GithubLoginStateGenerator() {
        this(new SecureRandom());
    }

    public GithubLoginStateGenerator(Random random) {
        this.random = Objects.requireNonNull(random);
        this.buffer = new char[LENGTH];
    }

    public String generate() {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));
        }

        return new String(buffer);
    }
}
