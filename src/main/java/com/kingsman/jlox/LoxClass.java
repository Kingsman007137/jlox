package com.kingsman.jlox;

import java.util.List;
import java.util.Map;

/**
 * Represents a Lox class in runtime.
 *
 * Use call expression to instantiate a class, so it needs to implement LoxCallable.
 */
class LoxClass implements LoxCallable {
    final String name;

    LoxClass(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter,
                       List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        return instance;
    }
}