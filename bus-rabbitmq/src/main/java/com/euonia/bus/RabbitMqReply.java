package com.euonia.bus;

/**
 * A wrapper class for RabbitMQ reply messages, encapsulating both the result and any potential error.
 * This class provides a standardized way to handle responses from RabbitMQ, allowing for easy error handling and result retrieval.
 *
 * @param <T> The type of the result contained in the reply.
 *
 * @author damon(zhaorong@outlook.com)
 */
public class RabbitMqReply<T> {
    private T result;
    private Throwable error;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public boolean isSuccess() {
        return error == null;
    }

    public static <T> RabbitMqReply<T> success(T result) {
        RabbitMqReply<T> reply = new RabbitMqReply<>();
        reply.setResult(result);
        return reply;
    }

    public static <T> RabbitMqReply<T> failure(Throwable error) {
        RabbitMqReply<T> reply = new RabbitMqReply<>();
        reply.setError(error);
        return reply;
    }
}
