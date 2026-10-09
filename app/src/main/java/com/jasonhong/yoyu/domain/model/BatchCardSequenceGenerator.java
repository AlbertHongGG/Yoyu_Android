package com.jasonhong.yoyu.domain.model;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pure domain utility generating consecutive card number sequences:
 * - Employs BigInteger arithmetic to completely eliminate numerical overflow.
 * - Guarantees exact digit-length preservation with leading zero padding.
 * - Enforces business boundaries (1 <= count <= 500, numeric validation).
 */
public final class BatchCardSequenceGenerator {

    public static final int MAX_BATCH_COUNT = 500;
    public static final int MIN_BATCH_COUNT = 1;

    private BatchCardSequenceGenerator() {}

    /**
     * Validates card number format and range boundaries.
     *
     * @throws IllegalArgumentException if input parameters violate constraints.
     */
    public static void validate(@NonNull String startCardNo, int count) {
        if (startCardNo == null || startCardNo.trim().isEmpty()) {
            throw new IllegalArgumentException("起始卡號不得為空");
        }
        String trimmed = startCardNo.trim();
        if (!trimmed.matches("^\\d+$")) {
            throw new IllegalArgumentException("卡號必須為純數字");
        }
        if (trimmed.length() != 11 && trimmed.length() != 16) {
            throw new IllegalArgumentException("一卡通卡號長度必須為 11 碼或 16 碼");
        }
        if (count < MIN_BATCH_COUNT) {
            throw new IllegalArgumentException("新增數量至少為 " + MIN_BATCH_COUNT + " 張");
        }
        if (count > MAX_BATCH_COUNT) {
            throw new IllegalArgumentException("單次批量新增上限為 " + MAX_BATCH_COUNT + " 張");
        }
    }

    /**
     * Calculates the terminal card number: S + N - 1.
     * E.g., start = 77050067379, count = 100 -> 77050067478.
     */
    @NonNull
    public static String calculateEndCardNo(@NonNull String startCardNo, int count) {
        validate(startCardNo, count);
        String trimmed = startCardNo.trim();
        int len = trimmed.length();
        BigInteger start = new BigInteger(trimmed);
        BigInteger end = start.add(BigInteger.valueOf(count - 1));
        return String.format(Locale.US, "%0" + len + "d", end);
    }

    /**
     * Generates an immutable list of consecutive card number strings.
     */
    @NonNull
    public static List<String> generate(@NonNull String startCardNo, int count) {
        validate(startCardNo, count);
        String trimmed = startCardNo.trim();
        int len = trimmed.length();
        BigInteger start = new BigInteger(trimmed);

        List<String> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            BigInteger current = start.add(BigInteger.valueOf(i));
            result.add(String.format(Locale.US, "%0" + len + "d", current));
        }
        return result;
    }
}
