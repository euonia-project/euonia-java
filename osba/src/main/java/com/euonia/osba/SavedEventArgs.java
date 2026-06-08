package com.euonia.osba;

public class SavedEventArgs {
    private final Object newObject;
    private Object userState;
    private Throwable error;

    public SavedEventArgs(Object newObject) {
        this.newObject = newObject;
    }

    public SavedEventArgs(Object newObject, Throwable error, Object userState) {
        this.newObject = newObject;
        this.error = error;
        this.userState = userState;
    }

    public Object getNewObject() {
        return newObject;
    }

    public Throwable getError() {
        return error;
    }

    public Object getUserState() {
        return userState;
    }
}
