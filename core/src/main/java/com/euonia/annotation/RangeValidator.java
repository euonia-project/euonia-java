package com.euonia.annotation;

import com.euonia.tuple.Duet;

public final class RangeValidator implements Validator<Range> {
    @Override
    public Duet<Boolean, String> validate(Range annotation, Object value) {
        boolean result = true;
        String message = null;

        if (value instanceof Number number) {
            if (number.doubleValue() > annotation.max()) {
                result = false;
                message = "Value is greater than max: " + annotation.max();
            } else if (number.doubleValue() < annotation.min()) {
                result = false;
                message = "Value is less than min: " + annotation.min();
            } else if (number.doubleValue() == annotation.min() && !annotation.inclusiveMin()) {
                result = false;
                message = "Value is equal to min but not inclusive: " + annotation.min();
            } else if (number.doubleValue() == annotation.max() && !annotation.inclusiveMax()) {
                result = false;
                message = "Value is equal to max but not inclusive: " + annotation.max();
            }
        }

        return new Duet<>(result, message);
    }
}
