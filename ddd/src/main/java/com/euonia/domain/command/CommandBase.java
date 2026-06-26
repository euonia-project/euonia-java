package com.euonia.domain.command;

import com.euonia.core.GuidType;
import com.euonia.core.ObjectId;
import com.euonia.reflection.TypeHelper;

import java.util.HashMap;
import java.util.Map;

public abstract class CommandBase implements Command {

    private final Map<String, String> properties = new HashMap<>();

    public Map<String, String> getProperties() {
        return properties;
    }

    public final String get(String property) {
        return properties.getOrDefault(property, null);
    }

    public final void set(String property, String value) {
        properties.put(property, value);
    }

    public <T> T get(String property, Class<T> castType) {
        var value = properties.getOrDefault(property, null);
        if (value == null) {
            return null;
        }
        return TypeHelper.coerceValue(castType, value);
    }
}
