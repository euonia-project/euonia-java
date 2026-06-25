package com.euonia.http;

public class RequestContextCopyingDecorator {

    public Runnable decorate(Runnable runnable) {
        var context = RequestContextAccessor.get();

        return () -> {
            var previous = RequestContextAccessor.get();
            try {
                RequestContextAccessor.set(context);
                runnable.run();
            } finally {
                if (previous != null) {
                    RequestContextAccessor.set(previous);
                } else {
                    RequestContextAccessor.remove();
                }
            }
        };
    }
}
