package com.openvehicletracking.collector.db;


/**
 * Created by oksuz on 28/09/2017.
 */
public class Result<T> {

    private boolean failed;
    private Throwable cause;
    private T result;

    public Result(T result, boolean failed, Throwable cause) {
        this.result = result;
        this.failed = failed;
        this.cause = cause;
    }

    public Result setCause(Throwable cause) {
        this.cause = cause;
        return this;
    }

    public Throwable getCause() {
        return cause;
    }

    public T getResult() {
        return result;
    }
}
