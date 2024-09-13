package com.kingsman.jlox;

import java.util.HashMap;
import java.util.Map;

/**
 * provides a way to store and retrieve variables
 */
public class Environment {
    // Using the raw string ensures all of those tokens refer to the same map key.
    public final Map<String, Object> values = new HashMap<>();

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

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }
}
