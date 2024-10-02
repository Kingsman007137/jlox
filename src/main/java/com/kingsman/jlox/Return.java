package com.kingsman.jlox;

/**
 * A custom exception to handle the return statement in Lox
 */
public class Return extends RuntimeException {
    final Object value;

    Return(Object value) {
        // we’re using our exception class for control flow and not actual
        // error handling, we don’t need overhead like stack traces.
        super(null, null, false, false);
        this.value = value;
    }
}
