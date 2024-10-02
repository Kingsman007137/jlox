package com.kingsman.jlox;

import java.util.List;

/**
 * Any Lox object that can be called like a function must implement this interface
 */
public interface LoxCallable {
    // The number of arguments the function expects
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
