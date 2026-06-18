package com.euonia.core;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * ULID（Universally Unique Lexicographically Sortable
 * Identifier，通用唯一字典序可排序标识符）生成器。
 * <p>
 * ULID 是一个 128 位的通用唯一标识符，具有字典序可排序和 URL 安全的特性。
 * </p>
 *
 * @author damon(zhaorong@outlook.com)
 */
public final class ULID {

    /**
     * ULID 使用一种特定的 32 字符编码，称为 Crockford Base32，
     * 包含数字和大写字母，但排除了 "I"、"L"、"O" 等字母，
     * 以避免与数字混淆。
     */
    private static final String CROCKFORD_BASE32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private ULID() {
        // 工具类，禁止实例化
    }

    /**
     * 生成一个新的 ULID 字符串。
     *
     * @return 一个 32 字符的 ULID 字符串
     */
    public static String generate() {
        byte[] timestamp = getTimestamp();
        byte[] randomBytes = getRandomBytes();
        return encode(timestamp, randomBytes);
    }

    /**
     * 获取当前 UTC 时间戳，作为 48 位（6 字节）大端序值。
     *
     * @return 6 字节，表示自 Unix 纪元以来的毫秒时间戳
     */
    private static byte[] getTimestamp() {
        long timestamp = Instant.now().toEpochMilli();
        // 转换为 8 字节大端序
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (timestamp >> 56);
        bytes[1] = (byte) (timestamp >> 48);
        bytes[2] = (byte) (timestamp >> 40);
        bytes[3] = (byte) (timestamp >> 32);
        bytes[4] = (byte) (timestamp >> 24);
        bytes[5] = (byte) (timestamp >> 16);
        bytes[6] = (byte) (timestamp >> 8);
        bytes[7] = (byte) timestamp;
        // 提取后 6 字节（低 48 位）
        byte[] result = new byte[6];
        System.arraycopy(bytes, 2, result, 0, 6);
        return result;
    }

    /**
     * 生成 10 个密码学安全的随机字节（80 位）。
     *
     * @return 10 个随机字节
     */
    private static byte[] getRandomBytes() {
        byte[] randomBytes = new byte[10];
        SECURE_RANDOM.nextBytes(randomBytes);
        return randomBytes;
    }

    /**
     * 将时间戳和随机字节编码为 Crockford Base32 格式的 ULID 字符串。
     *
     * @param timestamp   6 字节的时间戳数据
     * @param randomBytes 10 字节的随机数据
     * @return Base32 编码的 ULID 字符串
     */
    private static String encode(byte[] timestamp, byte[] randomBytes) {
        byte[] ulidBytes = new byte[16];
        System.arraycopy(timestamp, 0, ulidBytes, 0, 6);
        System.arraycopy(randomBytes, 0, ulidBytes, 6, 10);

        StringBuilder ulid = new StringBuilder(32);
        for (byte b : ulidBytes) {
            int value = b & 0xFF;
            ulid.append(CROCKFORD_BASE32.charAt((value >> 3) & 0x1F));
            ulid.append(CROCKFORD_BASE32.charAt(value & 0x1F));
        }
        return ulid.toString();
    }
}
