package com.euonia.http;

public class RequestContextCopyingDecorator {

    public Runnable decorate(Runnable runnable) {
        var context = DefaultRequestContextAccessor.get();

        return () -> {
            var previous = DefaultRequestContextAccessor.get();
            try {
                DefaultRequestContextAccessor.set(context);
                runnable.run();
            } finally {
                if (previous != null) {
                    DefaultRequestContextAccessor.set(previous);
                } else {
                    DefaultRequestContextAccessor.remove();
                }
            }
        };
    }
}
