package com.euonia.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RangeValidator - 区间表达式测试")
class RangeValidatorExpressionTest {

    private final RangeValidator validator = new RangeValidator();

    /**
     * 内部类提供带区间表达式的 @Range 注解样本。
     */
    private static class ExpressionSamples {

        @Range("[1,10]")
        private int inclusiveBoth;

        @Range("(1,10)")
        private int exclusiveBoth;

        @Range("[1,10)")
        private int inclusiveMinOnly;

        @Range("(1,10]")
        private int inclusiveMaxOnly;

        @Range("[-5,5]")
        private int negativeMin;

        @Range("[-10,-1]")
        private int negativeRange;

        @Range("[1.5,10.5]")
        private double decimalRange;

        @Range("[ 1 , 10 ]")
        private int spacesInExpression;

        @Range(value = "[1,100]", message = "custom range message")
        private int customMessage;

        @Range("[-3.14,3.14]")
        private double negativeToPositiveDecimal;
    }

    @Nested
    @DisplayName("区间表达式解析测试")
    class ExpressionParsing {

        @Test
        @DisplayName("给定 [1,10] 表达式，解析为 min=1, max=10, inclusiveMin=true, inclusiveMax=true")
        void givenInclusiveBothExpressionWhenParsingThenCorrect() throws NoSuchFieldException {
            Range annotation = ExpressionSamples.class
                    .getDeclaredField("inclusiveBoth")
                    .getAnnotation(Range.class);

            // min 边界内值通过
            Validator.Result result = validator.validate(annotation, 1);
            assertTrue(result.result(), "1 should be within [1,10]");
            assertNull(result.message());

            // max 边界内值通过
            result = validator.validate(annotation, 10);
            assertTrue(result.result(), "10 should be within [1,10]");
            assertNull(result.message());

            // 中间值通过
            result = validator.validate(annotation, 5);
            assertTrue(result.result(), "5 should be within [1,10]");
            assertNull(result.message());
        }

        @Test
        @DisplayName("给定 (1,10) 表达式，解析为 min=1, max=10, inclusiveMin=false, inclusiveMax=false")
        void givenExclusiveBothExpressionWhenParsingThenCorrect() throws NoSuchFieldException {
            Range annotation = ExpressionSamples.class
                    .getDeclaredField("exclusiveBoth")
                    .getAnnotation(Range.class);

            // 等于 min 边界值失败
            Validator.Result result = validator.validate(annotation, 1);
            assertFalse(result.result(), "1 should NOT be within (1,10)");
            assertNotNull(result.message());

            // 等于 max 边界值失败
            result = validator.validate(annotation, 10);
            assertFalse(result.result(), "10 should NOT be within (1,10)");
            assertNotNull(result.message());

            // 中间值通过
            result = validator.validate(annotation, 5);
            assertTrue(result.result(), "5 should be within (1,10)");
            assertNull(result.message());
        }

        @Test
        @DisplayName("给定 [1,10) 表达式，解析为 min=1 inclusive, max=10 exclusive")
        void givenInclusiveMinOnlyExpressionWhenParsingThenCorrect() throws NoSuchFieldException {
            Range annotation = ExpressionSamples.class
                    .getDeclaredField("inclusiveMinOnly")
                    .getAnnotation(Range.class);

            // min 边界值通过
            Validator.Result result = validator.validate(annotation, 1);
            assertTrue(result.result(), "1 should be within [1,10)");
            assertNull(result.message());

            // max 边界值失败
            result = validator.validate(annotation, 10);
            assertFalse(result.result(), "10 should NOT be within [1,10)");
            assertNotNull(result.message());

            // 中间值通过
            result = validator.validate(annotation, 5);
            assertTrue(result.result(), "5 should be within [1,10)");
            assertNull(result.message());
        }

        @Test
        @DisplayName("给定 (1,10] 表达式，解析为 min=1 exclusive, max=10 inclusive")
        void givenInclusiveMaxOnlyExpressionWhenParsingThenCorrect() throws NoSuchFieldException {
            Range annotation = ExpressionSamples.class
                    .getDeclaredField("inclusiveMaxOnly")
                    .getAnnotation(Range.class);

            // min 边界值失败
            Validator.Result result = validator.validate(annotation, 1);
            assertFalse(result.result(), "1 should NOT be within (1,10]");
            assertNotNull(result.message());

            // max 边界值通过
            result = validator.validate(annotation, 10);
            assertTrue(result.result(), "10 should be within (1,10]");
            assertNull(result.message());

            // 中间值通过
            result = validator.validate(annotation, 5);
            assertTrue(result.result(), "5 should be within (1,10]");
            assertNull(result.message());
        }
    }

    @Nested
    @DisplayName("负数区间测试")
    class NegativeRange {

        @Test
        @DisplayName("给定 [-5,5] 表达式，跨零区间正常验证")
        void givenNegativeToPositiveRangeWhenValidatingThenCorrect() throws NoSuchFieldException {
            Range annotation = ExpressionSamples.class
                    .getDeclaredField("negativeMin")
                    .getAnnotation(Range.class);

            // 负边界通过
            Validator.Result result = validator.validate(annotation, -5);
            assertTrue(result.result(), "-5 should be within [-5,5]");

            // 正边界通过
            result = validator.validate(annotation, 5);
            assertTrue(result.result(), "5 should be within [-5,5]");

            // 零通过
            result = validator.validate(annotation, 0);
            assertTrue(result.result(), "0 should be within [-5,5]");

            // 超出下界
            result = validator.validate(annotation, -6);
            assertFalse(result.result(), "-6 should NOT be within [-5,5]");

            // 超出上界
            result = validator.validate(annotation, 6);
            assertFalse(result.result(), "6 should NOT be within [-5,5]");
        }

        @Test
        @DisplayName("给定 [-10,-1] 表达式，纯负数区间正常验证")
        void givenPureNegativeRangeWhenValidatingThenCorrect() throws NoSuchFieldException {
            Range annotation = ExpressionSamples.class
                    .getDeclaredField("negativeRange")
                    .getAnnotation(Range.class);

            Validator.Result result = validator.validate(annotation, -10);
            assertTrue(result.result(), "-10 should be within [-10,-1]");

            result = validator.validate(annotation, -1);
            assertTrue(result.result(), "-1 should be within [-10,-1]");

            result = validator.validate(annotation, -5);
            assertTrue(result.result(), "-5 should be within [-10,-1]");

            result = validator.validate(annotation, 0);
            assertFalse(result.result(), "0 should NOT be within [-10,-1]");
        }
    }

    @Nested
    @DisplayName("小数区间测试")
    class DecimalRange {

        @Test
        @DisplayName("给定 [1.5,10.5] 表达式，小数边界正常验证")
        void givenDecimalRangeWhenValidatingThenCorrect() throws NoSuchFieldException {
            Range annotation = ExpressionSamples.class
                    .getDeclaredField("decimalRange")
                    .getAnnotation(Range.class);

            Validator.Result result = validator.validate(annotation, 1.5);
            assertTrue(result.result(), "1.5 should be within [1.5,10.5]");

            result = validator.validate(annotation, 10.5);
            assertTrue(result.result(), "10.5 should be within [1.5,10.5]");

            result = validator.validate(annotation, 5.7);
            assertTrue(result.result(), "5.7 should be within [1.5,10.5]");

            result = validator.validate(annotation, 1.4);
            assertFalse(result.result(), "1.4 should NOT be within [1.5,10.5]");

            result = validator.validate(annotation, 10.6);
            assertFalse(result.result(), "10.6 should NOT be within [1.5,10.5]");
        }

        @Test
        @DisplayName("给定 [-3.14,3.14] 表达式，负载到正数小数区间")
        void givenNegativeToPositiveDecimalRangeWhenValidatingThenCorrect() throws NoSuchFieldException {
            Range annotation = ExpressionSamples.class
                    .getDeclaredField("negativeToPositiveDecimal")
                    .getAnnotation(Range.class);

            Validator.Result result = validator.validate(annotation, -3.14);
            assertTrue(result.result(), "-3.14 should be within [-3.14,3.14]");

            result = validator.validate(annotation, 3.14);
            assertTrue(result.result(), "3.14 should be within [-3.14,3.14]");

            result = validator.validate(annotation, 0.0);
            assertTrue(result.result(), "0.0 should be within [-3.14,3.14]");

            result = validator.validate(annotation, -3.15);
            assertFalse(result.result(), "-3.15 should NOT be within [-3.14,3.14]");
        }
    }

    @Nested
    @DisplayName("表达式带空格测试")
    class ExpressionWithSpaces {

        @Test
        @DisplayName("给定 [ 1 , 10 ] 表达式（含空格），正确解析并验证")
        void givenExpressionWithSpacesWhenValidatingThenCorrect() throws NoSuchFieldException {
            Range annotation = ExpressionSamples.class
                    .getDeclaredField("spacesInExpression")
                    .getAnnotation(Range.class);

            Validator.Result result = validator.validate(annotation, 1);
            assertTrue(result.result(), "1 should be within the spaced expression range");

            result = validator.validate(annotation, 10);
            assertTrue(result.result(), "10 should be within the spaced expression range");

            result = validator.validate(annotation, 11);
            assertFalse(result.result(), "11 should NOT be within the spaced expression range");
        }
    }

    @Nested
    @DisplayName("区间表达式 + 自定义消息测试")
    class ExpressionWithCustomMessage {

        @Test
        @DisplayName("给定区间表达式和自定义消息，验证失败时返回自定义消息")
        void givenExpressionAndCustomMessageWhenValidationFailsThenReturnCustomMessage()
                throws NoSuchFieldException {
            Range annotation = ExpressionSamples.class
                    .getDeclaredField("customMessage")
                    .getAnnotation(Range.class);

            Validator.Result result = validator.validate(annotation, 1000);
            assertFalse(result.result());
            assertEquals("custom range message", result.message());
        }
    }

    @Nested
    @DisplayName("非数字值测试")
    class NonNumericValue {

        @Test
        @DisplayName("给定非数字值(字符串)，验证直接通过")
        void givenStringValueWhenValidatingThenSucceed() throws NoSuchFieldException {
            Range annotation = ExpressionSamples.class
                    .getDeclaredField("inclusiveBoth")
                    .getAnnotation(Range.class);

            Validator.Result result = validator.validate(annotation, "not a number");
            assertTrue(result.result(), "String value should pass validation");
            assertNull(result.message());
        }

        @Test
        @DisplayName("给定 null 值，验证直接通过")
        void givenNullValueWhenValidatingThenSucceed() throws NoSuchFieldException {
            Range annotation = ExpressionSamples.class
                    .getDeclaredField("inclusiveBoth")
                    .getAnnotation(Range.class);

            Validator.Result result = validator.validate(annotation, null);
            assertTrue(result.result(), "null value should pass validation");
            assertNull(result.message());
        }
    }
}
