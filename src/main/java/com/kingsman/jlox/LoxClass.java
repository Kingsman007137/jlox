package com.kingsman.jlox;

import java.util.List;
import java.util.Map;

/**
 * Represents a Lox class in runtime, stores behavior(methods).
 *
 * Use call expression to instantiate a class, so it needs to implement LoxCallable.
 */
class LoxClass implements LoxCallable {
    final String name;
    final Map<String, LoxFunction> methods;

    LoxClass(String name, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        // If there is an initializer, that method’s arity determines how
        // many arguments you must pass when you call the class itself.
        LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter,
                       List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            // bind and invoke it just like a normal method call.
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    public LoxFunction findMethod(String lexeme) {
        if (methods.containsKey(lexeme)) {
            return methods.get(lexeme);
        }

        return null;
    }
}