package com.kingsman.jlox;

/**
 * Represents an instance of Lox class in runtime.
 */
class LoxInstance {
    private LoxClass klass;

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}