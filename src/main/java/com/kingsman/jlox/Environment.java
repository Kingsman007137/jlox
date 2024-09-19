package com.kingsman.jlox;

import java.util.HashMap;
import java.util.Map;

/**
 * provides a way to store and retrieve variables
 */
public class Environment {
    // we chain environments, give each environment a reference to its enclosing one,
    // walk that chain from innermost out until we find the variable
    final Environment enclosing;
    // Using the raw string ensures all of those tokens refer to the same map key.
    public final Map<String, Object> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * store the value of a variable in the environment
     */
    void define(String name, Object value) {
        values.put(name, value);
    }

    /**
     * get the value of a variable from the environment
     */
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        // if the variable is not found in the current environment, check the enclosing one
        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    /**
     * assign a new value to a variable
     */
    void assign(Token name, Object value) {
        // assignment is not allowed to create a new variable
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        // if the variable is not found in the current environment, check the enclosing one
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }
}
