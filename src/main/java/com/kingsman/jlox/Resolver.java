package com.kingsman.jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Resolves variable references - tracks down which declaration it refers to
 *
 * After the parser produces the syntax tree, but before the interpreter starts
 * executing it, we’ll do a single walk over the tree to resolve all the
 * variables it contains.
 *
 * inspects the user’s program, finds every variable mentioned, and figures out
 * which declaration each refers to - an example of semantic analysis.
 */
public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    // Each map represents a block scope, and maps variable names to whether they have been defined.
    // The scope stack is only used for local block scopes, if we can’t find it in the stack of local
    // scopes, we assume it must be global.
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Resolves the variable references in the given list of statements
     *
     * @param statements
     */
    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        // define the name eagerly, before resolving the function’s body.
        // if we don’t, we could not call the function recursively.
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        // Since every branch could be reached at runtime, we resolve both.
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value != null) {
            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        // split declaration and definition to avoid some edge cases
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        // resolve the condition and the body exactly once
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        // literal expression doesn’t mention any variables and
        // doesn’t contain any subexpressions so there is no work to do
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() &&
                scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            // that means we have declared it but not yet defined it
            Lox.error(expr.name,
                    "Can't read local variable in its own initializer.");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    /**
     * these two methods apply the Visitor pattern to the given syntax tree node
     */
    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }

    /**
     * adds the variable to the innermost scope so that it shadows
     * any outer one and so that we know the variable exists
     *
     * @param name
     */
    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();
        // value associated with a key in the scope map represents
        // whether we have finished resolving that variable’s initializer.
        // so here is false.
        scope.put(name.lexeme, false);
    }

    /**
     * set value to true to mark it as fully initialized and available for use
     *
     * @param name
     */
    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }

    /**
     * starts from the innermost scope and searches outward for a variable
     * with the matching name.
     *
     * @param expr
     * @param name
     */
    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                // pass the expression and the distance from the
                // innermost scope to where the variable was found.
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
        // If not found. Assume it is global.
    }

    /**
     * Unlike interpreting, in a static analysis, we immediately traverse into
     * the body right then and there.
     *
     * @param function
     */
    private void resolveFunction(Stmt.Function function) {
        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
    }
}
