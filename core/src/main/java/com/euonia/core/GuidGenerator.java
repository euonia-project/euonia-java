package com.euonia.core;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * 使用与框架 .NET GUID 生成模式相匹配的布局生成 UUID 值。
 *
 * @author damon(zhaorong@outlook)
 */
public final class GuidGenerator {

    private static final long DOTNET_EPOCH_OFFSET_MILLIS = 62_135_596_800_000L;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private GuidGenerator() {
        // 工具类
    }

    /**
     * 使用指定的 GUID 生成模式创建 UUID。
     *
     * @param type 请求的 GUID 类型
     * @return 生成的 UUID
     */
    public static UUID generate(GuidType type) {
        if (type == GuidType.EMPTY) {
            return new UUID(0L, 0L);
        }

        if (type == GuidType.DEFAULT) {
            return UUID.randomUUID();
        }

        byte[] randomBytes = new byte[10];
        SECURE_RANDOM.nextBytes(randomBytes);

        byte[] timestampBytes = getTimestampBytes();
        byte[] guidBytes = new byte[16];

        switch (type) {
            case SEQUENTIAL_AS_STRING:
            case SEQUENTIAL_AS_BINARY:
                System.arraycopy(timestampBytes, 2, guidBytes, 0, 6);
                System.arraycopy(randomBytes, 0, guidBytes, 6, 10);

                if (type == GuidType.SEQUENTIAL_AS_STRING) {
                    reverse(guidBytes, 0, 4);
                    reverse(guidBytes, 4, 2);
                }
                break;

            case SEQUENTIAL_AT_END:
                System.arraycopy(randomBytes, 0, guidBytes, 0, 10);
                System.arraycopy(timestampBytes, 2, guidBytes, 10, 6);
                break;

            default:
                throw new IllegalArgumentException("不支持的 GuidType: " + type);
        }

        return fromDotNetBytes(guidBytes);
    }

    private static byte[] getTimestampBytes() {
        long timestamp = System.currentTimeMillis() + DOTNET_EPOCH_OFFSET_MILLIS;
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (timestamp >> 56);
        bytes[1] = (byte) (timestamp >> 48);
        bytes[2] = (byte) (timestamp >> 40);
        bytes[3] = (byte) (timestamp >> 32);
        bytes[4] = (byte) (timestamp >> 24);
        bytes[5] = (byte) (timestamp >> 16);
        bytes[6] = (byte) (timestamp >> 8);
        bytes[7] = (byte) timestamp;
        return bytes;
    }

    private static UUID fromDotNetBytes(byte[] dotNetBytes) {
        byte[] uuidBytes = dotNetBytes.clone();
        reverse(uuidBytes, 0, 4);
        reverse(uuidBytes, 4, 2);
        reverse(uuidBytes, 6, 2);

        ByteBuffer buffer = ByteBuffer.wrap(uuidBytes);
        long mostSignificantBits = buffer.getLong();
        long leastSignificantBits = buffer.getLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    private static void reverse(byte[] bytes, int offset, int length) {
        for (int left = offset, right = offset + length - 1; left < right; left++, right--) {
            byte current = bytes[left];
            bytes[left] = bytes[right];
            bytes[right] = current;
        }
    }
}
