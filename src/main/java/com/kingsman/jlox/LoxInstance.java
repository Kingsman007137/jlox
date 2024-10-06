package com.kingsman.jlox;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an instance of Lox class in runtime, stores state(fields).
 *
 * Every instance is an open collection of named values.
 */
class LoxInstance {
    private LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }

    /**
     * get the value of a property from the instance
     */
    public Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        LoxFunction method = klass.findMethod(name.lexeme);
        if (method != null) return method.bind(this);

        throw new RuntimeError(name,
                "Undefined property '" + name.lexeme + "'.");
    }

    /**
     * assign a new value to a property
     */
    public void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }
}