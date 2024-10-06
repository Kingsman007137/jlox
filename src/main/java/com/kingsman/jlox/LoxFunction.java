package com.kingsman.jlox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    // holds on to the surrounding variables where the function is declared.
    // use to implement local function scope.
    private final Environment closure;
    private final boolean isInitializer;

    LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter,
                       List<Object> arguments) {
        // This creates an environment chain that goes from the function’s body out
        // through the environments where the function is declared, all the way out
        // to the global scope.
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme,
                    arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            // If the function is an initializer, return the instance.
            if (isInitializer) return closure.getAt(0, "this");

            return returnValue.value;
        }

        if (isInitializer) return closure.getAt(0, "this");

        // If it doesn't catch anything, that means the function doesn't
        // explicitly return a value, we implicitly return nil.
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }

    /**
     * create a new environment nestled inside the method’s original closure.
     * when the method is called, that will become the parent of the method body’s environment.
     * @param loxInstance
     * @return
     */
    public LoxFunction bind(LoxInstance loxInstance) {
        Environment environment = new Environment(closure);
        environment.define("this", loxInstance);
        return new LoxFunction(declaration, environment,
                isInitializer);
    }
}
