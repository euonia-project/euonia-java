package com.euonia.pipeline;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Pipeline {
    Pipeline use(Function<PipelineDelegate, PipelineDelegate> component);

    Pipeline use(Function<PipelineDelegate, PipelineDelegate> component, int index);

    Pipeline use(BiFunction<Object, PipelineDelegate, CompletionStage<Void>> handler);

    Pipeline use(Class<?> type, Object... args);

    Pipeline useOf(Class<?> contextType, boolean useAheadOfOthers);

    PipelineDelegate build();

    CompletionStage<Void> runAsync(Object context);

    CompletionStage<Void> runAsync(Object context, Function<Object, CompletionStage<Void>> accumulate);
}
