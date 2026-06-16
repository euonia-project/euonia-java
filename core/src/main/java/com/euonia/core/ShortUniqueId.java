package com.euonia.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ShortUniqueId 是一个工具类，用于从整数或长整数生成短且唯一的字符串 ID。
 * 它提供了将整数、长整数和十六进制字符串编码为短唯一 ID 以及反向解码的方法。
 * 该类允许自定义编码过程中使用的字母表、分隔符、守卫字符和盐值，以确保唯一性和安全性。
 * 该实现基于 Hashids 算法，该算法旨在创建短小、非连续且 URL 友好的 ID。
 *
 * @author damon(zhaorong@outlook)
 */
@SuppressWarnings("unused")
public final class ShortUniqueId {
    private static final String DEFAULT_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final String DEFAULT_SEPS = "cfhistuCFHISTU";
    private static final int MIN_ALPHABET_LENGTH = 16;
    private static final int MAX_STACK_ALLOC_SIZE = 512;
    private static final double SEP_DIV = 3.5;
    private static final double GUARD_DIV = 12.0;

    private final char[] alphabet;
    private final char[] seps;
    private final char[] guards;
    private final char[] salt;
    private final int minHashLength;
    private final int minBufferSize;

    private static final Pattern HEX_VALIDATOR = Pattern.compile("^[0-9a-fA-F]+$");
    private static final Pattern HEX_SPLITTER = Pattern.compile(".{1,12}");

    private static final ShortUniqueId DEFAULT = new ShortUniqueId();

    public static ShortUniqueId getDefault() {
        return DEFAULT;
    }

    public ShortUniqueId() {
        this("", 0, DEFAULT_ALPHABET, DEFAULT_SEPS);
    }

    public ShortUniqueId(String salt, int minHashLength, String alphabet, String seps) {
        if (salt == null)
            throw new IllegalArgumentException("salt");
        if (minHashLength < 0)
            throw new IllegalArgumentException("minHashLength must be >= 0");
        if (alphabet == null || alphabet.isBlank())
            throw new IllegalArgumentException("alphabet");
        if (seps == null || seps.isBlank())
            throw new IllegalArgumentException("seps");

        this.salt = salt.trim().toCharArray();
        this.minHashLength = minHashLength;

        // 字母表去重
        StringBuilder alphaBuilder = new StringBuilder();
        for (char c : alphabet.toCharArray())
            if (alphaBuilder.indexOf(String.valueOf(c)) == -1)
                alphaBuilder.append(c);
        char[] alpha = toCharArray(alphaBuilder.toString());

        char[] sepsArr = toCharArray(seps);

        this.minBufferSize = Math.max(20, minHashLength);

        if (alpha.length < MIN_ALPHABET_LENGTH)
            throw new IllegalArgumentException(
                    "Alphabet must contain at least " + MIN_ALPHABET_LENGTH + " unique characters.");

        // 分隔符必须来自字母表
        if (sepsArr.length > 0) {
            List<Character> tmp = new ArrayList<>();
            for (char c : sepsArr) {
                for (char a : alpha)
                    if (a == c) {
                        tmp.add(c);
                        break;
                    }
            }
            sepsArr = toCharArray(listToString(tmp));
        }

        // 从字母表中移除分隔符
        if (sepsArr.length > 0) {
            List<Character> newAlpha = new ArrayList<>();
            for (char a : alpha) {
                boolean ok = true;
                for (char s : sepsArr)
                    if (a == s) {
                        ok = false;
                        break;
                    }
                if (ok)
                    newAlpha.add(a);
            }
            alpha = listToCharArray(newAlpha);
        }

        if (alpha.length < (MIN_ALPHABET_LENGTH - 6))
            throw new IllegalArgumentException("Alphabet must contain at least " + (MIN_ALPHABET_LENGTH - 6)
                    + " unique characters not present in seps.");

        // 使用盐值对分隔符进行一致性洗牌
        consistentShuffle(sepsArr, this.salt);

        if (sepsArr.length == 0 || ((float) alpha.length / sepsArr.length) > SEP_DIV) {
            int sepsLength = (int) Math.ceil((float) alpha.length / SEP_DIV);
            if (sepsLength == 1)
                sepsLength = 2;
            if (sepsLength > sepsArr.length) {
                int diff = sepsLength - sepsArr.length;
                sepsArr = append(sepsArr, alpha, diff);
                alpha = subArray(alpha, diff);
            } else {
                sepsArr = subArray(sepsArr, 0, sepsLength);
            }
        }

        consistentShuffle(alpha, this.salt);

        int guardCount = (int) Math.ceil(alpha.length / GUARD_DIV);
        char[] guardsArr;
        if (alpha.length < 3) {
            guardsArr = subArray(sepsArr, 0, guardCount);
            sepsArr = subArray(sepsArr, guardCount);
        } else {
            guardsArr = subArray(alpha, 0, guardCount);
            alpha = subArray(alpha, guardCount);
        }

        this.alphabet = alpha;
        this.seps = sepsArr;
        this.guards = guardsArr;
    }

    // 公共编码/解码方法

    /**
     * 将单个整数编码为短唯一字符串 ID。
     *
     * @param number 要编码的整数
     * @return 表示输入整数的短唯一字符串 ID
     */
    public String encode(int number) {
        return encode(new long[] { number });
    }

    /**
     * 将一个或多个整数编码为短唯一字符串 ID。
     *
     * @param numbers 要编码的整数
     * @return 表示输入整数的短唯一字符串 ID
     */
    public String encode(int... numbers) {
        long[] arr = new long[numbers.length];
        for (int i = 0; i < numbers.length; i++)
            arr[i] = numbers[i];
        return encode(arr);
    }

    /**
     * 将整数集合编码为短唯一字符串 ID。
     *
     * @param numbers 要编码的整数集合
     * @return 表示输入整数的短唯一字符串 ID
     */
    public String encode(Collection<Integer> numbers) {
        long[] arr = numbers.stream().mapToLong(Integer::longValue).toArray();
        return encode(arr);
    }

    /**
     * 将单个长整数编码为短唯一字符串 ID。
     *
     * @param number 要编码的长整数
     * @return 表示输入长整数的短唯一字符串 ID
     */
    public String encode(long number) {
        return encode(new long[] { number });
    }

    /**
     * 将一个或多个长整数编码为短唯一字符串 ID。
     *
     * @param numbers 要编码的长整数
     * @return 表示输入长整数的短唯一字符串 ID
     */
    public String encode(long... numbers) {
        if (numbers == null || numbers.length == 0)
            return "";
        for (long n : numbers)
            if (n < 0)
                return "";

        long numbersHashInt = 0;
        for (int i = 0; i < numbers.length; i++)
            numbersHashInt += numbers[i] % (i + 100);

        StringBuilder sb = new StringBuilder();

        char[] alphabetCopy = Arrays.copyOf(this.alphabet, this.alphabet.length);

        char lottery = alphabetCopy[(int) (numbersHashInt % alphabetCopy.length)];
        sb.append(lottery);

        char[] buffer = new char[alphabetCopy.length + this.salt.length + 1];
        buffer[0] = lottery;
        System.arraycopy(this.salt, 0, buffer, 1, Math.min(this.salt.length, alphabetCopy.length - 1));

        int startIndex = 1 + this.salt.length;
        int length = alphabetCopy.length - startIndex;

        for (int i = 0; i < numbers.length; i++) {
            long number = numbers[i];

            if (length > 0)
                System.arraycopy(alphabetCopy, 0, buffer, startIndex, length);

            consistentShuffle(alphabetCopy, buffer);
            char[] hashBuffer = new char[this.minBufferSize];
            int hashLength = buildReversedHash(number, alphabetCopy, hashBuffer);

            // 反向追加
            for (int j = hashLength - 1; j >= 0; j--)
                sb.append(hashBuffer[j]);

            if (i + 1 < numbers.length) {
                number %= hashBuffer[Math.max(0, hashLength - 1)] + i;
                int sepsIndex = (int) (number % this.seps.length);
                sb.append(this.seps[sepsIndex]);
            }
        }

        if (sb.length() < this.minHashLength) {
            int guardIndex = (int) ((numbersHashInt + sb.charAt(0)) % this.guards.length);
            char guard = this.guards[guardIndex];
            sb.insert(0, guard);
            if (sb.length() < this.minHashLength) {
                guardIndex = (int) ((numbersHashInt + sb.charAt(2)) % this.guards.length);
                guard = this.guards[guardIndex];
                sb.append(guard);
            }
        }

        int halfLength = this.alphabet.length / 2;
        while (sb.length() < this.minHashLength) {
            char[] tmp = Arrays.copyOf(this.alphabet, this.alphabet.length);
            consistentShuffle(tmp, buffer);
            sb.insert(0, new String(tmp, halfLength, tmp.length - halfLength));
            sb.append(new String(tmp, 0, halfLength));

            int excess = sb.length() - this.minHashLength;
            if (excess > 0) {
                sb.delete(0, excess / 2);
                if (sb.length() > this.minHashLength)
                    sb.delete(this.minHashLength, sb.length());
            }
        }

        return sb.toString();
    }

    /**
     * 将短唯一字符串 ID 解码回原始整数。
     *
     * @param hash 要解码的短唯一字符串 ID
     * @return 表示原始值的整数数组
     */
    public int[] decode(String hash) {
        long[] longs = decodeLong(hash);
        int[] res = new int[longs.length];
        for (int i = 0; i < longs.length; i++) {
            res[i] = (int) longs[i];
        }
        return res;
    }

    /**
     * 将短唯一字符串 ID 解码回原始长整数。
     *
     * @param hash 要解码的短唯一字符串 ID
     * @return 表示原始值的长整数数组
     */
    public long[] decodeLong(String hash) {
        if (hash == null || hash.isBlank()) {
            return new long[0];
        }
        return numbersFrom(hash);
    }

    /**
     * 将十六进制字符串编码为短唯一字符串 ID。输入的十六进制字符串被分割为每 12 个字符一块，
     * 每块前缀添加 '1' 以确保其可以被解析为有效的长整数，然后使用标准编码方法进行编码。
     *
     * @param hex 要编码的十六进制字符串
     * @return 表示输入十六进制字符串的短唯一字符串 ID，如果输入无效则返回空字符串
     */
    public String encodeHex(String hex) {
        if (hex == null || hex.isBlank() || !HEX_VALIDATOR.matcher(hex).matches())
            return "";
        Matcher m = HEX_SPLITTER.matcher(hex);
        List<Long> numbers = new ArrayList<>();
        while (m.find()) {
            String match = m.group();
            String concat = "1" + match;
            long number = Long.parseLong(concat, 16);
            numbers.add(number);
        }
        long[] arr = numbers.stream().mapToLong(Long::longValue).toArray();
        return encode(arr);
    }

    /**
     * 将短唯一字符串 ID 解码回原始十六进制字符串。解码后的长整数被转换回十六进制字符串，
     * 拼接在一起后作为最终结果返回。
     *
     * @param hash 要解码的短唯一字符串 ID
     * @return 输入 ID 所表示的原始十六进制字符串，如果输入无效则返回空字符串
     */
    public String decodeHex(String hash) {
        long[] numbers = decodeLong(hash);
        StringBuilder sb = new StringBuilder();
        for (long number : numbers) {
            String s = Long.toHexString(number).toUpperCase(Locale.ROOT);
            for (int i = 1; i < s.length(); i++)
                sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    // 内部辅助方法
    private int buildReversedHash(long input, char[] alphabet, char[] hashBuffer) {
        int length = 0;
        do {
            int idx = (int) (input % alphabet.length);
            hashBuffer[length++] = alphabet[idx];
            input /= alphabet.length;
        } while (input > 0 && length < hashBuffer.length);
        return length;
    }

    private long unhash(char[] input, char[] alphabet) {
        long number = 0;
        for (char c : input) {
            int pos = indexOf(alphabet, c);
            number = (number * this.alphabet.length) + pos;
        }
        return number;
    }

    private long getNumberFrom(String hash) {
        if (hash == null || hash.isBlank())
            return -1;
        // 分割守卫字符
        SplitResult guarded = split(hash, this.guards);
        int unguardedIndex = (guarded.count == 3 || guarded.count == 2) ? 1 : 0;
        Range r = guarded.ranges[unguardedIndex];
        String hashBreakdown = hash.substring(r.start, r.start + r.length);
        char lottery = hashBreakdown.charAt(0);
        if (lottery == '\0')
            return -1;

        String hashBuffer = hashBreakdown.substring(1);
        char[] alphabetCopy = Arrays.copyOf(this.alphabet, this.alphabet.length);

        char[] buffer = new char[alphabetCopy.length + this.salt.length + 1];
        buffer[0] = lottery;
        if (Math.min(this.salt.length, alphabetCopy.length - 1) >= 0) {
            System.arraycopy(this.salt, 0, buffer, 1, Math.min(this.salt.length, alphabetCopy.length - 1));
        }

        int startIndex = 1 + this.salt.length;
        int length = alphabetCopy.length - startIndex;

        if (length > 0) {
            System.arraycopy(alphabetCopy, 0, buffer, startIndex, length);
        }
        consistentShuffle(alphabetCopy, buffer);
        long result = unhash(hashBuffer.toCharArray(), alphabetCopy);

        // 重新生成并比对
        String rehash = encode(result);
        if (hash.equals(rehash))
            return result;
        return -1;
    }

    private long[] numbersFrom(String hash) {
        if (hash == null || hash.isBlank())
            return new long[0];

        SplitResult guarded = split(hash, this.guards);
        if (guarded.count == 0)
            return new long[0];
        int unguardedIndex = (guarded.count == 3 || guarded.count == 2) ? 1 : 0;
        Range rg = guarded.ranges[unguardedIndex];
        String hashBreakdown = hash.substring(rg.start, rg.start + rg.length);

        if (hashBreakdown.isEmpty())
            return new long[0];

        char lottery = hashBreakdown.charAt(0);
        if (lottery == '\0')
            return new long[0];

        String hashBuffer = hashBreakdown.substring(1);
        SplitResult split = split(hashBuffer, this.seps);
        int parts = split.count;
        long[] result = new long[parts];

        char[] alphabetCopy = Arrays.copyOf(this.alphabet, this.alphabet.length);

        char[] buffer = new char[alphabetCopy.length + this.salt.length + 1];
        buffer[0] = lottery;
        if (Math.min(this.salt.length, alphabetCopy.length - 1) >= 0) {
            System.arraycopy(this.salt, 0, buffer, 1, Math.min(this.salt.length, alphabetCopy.length - 1));
        }

        int startIndex = 1 + this.salt.length;
        int length = alphabetCopy.length - startIndex;

        for (int index = 0; index < parts; index++) {
            Range rr = split.ranges[index];
            String subHash = hashBuffer.substring(rr.start, rr.start + rr.length);
            if (length > 0) {
                System.arraycopy(alphabetCopy, 0, buffer, startIndex, length);
            }
            consistentShuffle(alphabetCopy, buffer);
            result[index] = unhash(subHash.toCharArray(), alphabetCopy);
        }

        // 校验
        String rehash = encode(result);
        if (hash.equals(rehash))
            return result;
        return new long[0];
    }

    // 工具函数
    private static char[] toCharArray(String s) {
        return s == null ? new char[0] : s.toCharArray();
    }

    private static String listToString(List<Character> list) {
        StringBuilder sb = new StringBuilder();
        for (char c : list)
            sb.append(c);
        return sb.toString();
    }

    private static char[] listToCharArray(List<Character> list) {
        char[] a = new char[list.size()];
        for (int i = 0; i < list.size(); i++)
            a[i] = list.get(i);
        return a;
    }

    private static int indexOf(char[] arr, char c) {
        for (int i = 0; i < arr.length; i++)
            if (arr[i] == c)
                return i;
        return -1;
    }

    private static void consistentShuffle(char[] alphabet, char[] salt) {
        if (salt == null || salt.length == 0)
            return;
        for (int i = alphabet.length - 1, v = 0, p = 0; i > 0; i--, v++) {
            v %= salt.length;
            int saltNum = salt[v];
            p += saltNum;
            int j = (saltNum + v + p) % i;
            // 交换
            char tmp = alphabet[i];
            alphabet[i] = alphabet[j];
            alphabet[j] = tmp;
        }
    }

    private static char[] subArray(char[] array, int index) {
        return subArray(array, index, array.length - index);
    }

    private static char[] subArray(char[] array, int index, int length) {
        if (length == 0)
            return new char[0];
        char[] sub = new char[length];
        System.arraycopy(array, index, sub, 0, length);
        return sub;
    }

    private static char[] append(char[] array, char[] appendArray, int length) {
        if (length == 0) {
            return array;
        }
        int newLength = array.length + length;
        if (newLength == 0) {
            return new char[0];
        }
        char[] newArr = new char[newLength];
        System.arraycopy(array, 0, newArr, 0, array.length);
        System.arraycopy(appendArray, 0, newArr, array.length, length);
        return newArr;
    }

    // 按分隔符分割：返回任意分隔符之间的子串范围
    private static class Range {
        int start;
        int length;

        Range(int s, int l) {
            start = s;
            length = l;
        }
    }

    private static class SplitResult {
        int count;
        Range[] ranges;

        SplitResult(int c, Range[] r) {
            count = c;
            ranges = r;
        }
    }

    private SplitResult split(String line, char[] separators) {
        int count = 0;
        int indexStart = 0;
        int nextSeparatorIndex = 0;
        Range[] ranges = new Range[line.length()];
        boolean isLastLoop = false;
        while (!isLastLoop) {
            indexStart += nextSeparatorIndex;
            nextSeparatorIndex = indexOfAny(line, indexStart, separators);
            if (nextSeparatorIndex == 0) {
                indexStart++;
                nextSeparatorIndex = indexOfAny(line, indexStart, separators);
            }
            isLastLoop = nextSeparatorIndex == -1;
            if (isLastLoop)
                nextSeparatorIndex = line.length() - indexStart;
            String slice = line.substring(indexStart, indexStart + nextSeparatorIndex);
            if (slice.isEmpty())
                continue;
            ranges[count++] = new Range(indexStart, nextSeparatorIndex);
        }
        return new SplitResult(count, Arrays.copyOf(ranges, count));
    }

    private int indexOfAny(String s, int from, char[] any) {
        for (int i = from; i < s.length(); i++) {
            char c = s.charAt(i);
            for (char sep : any)
                if (c == sep)
                    return i - from;
        }
        return -1;
    }
}
