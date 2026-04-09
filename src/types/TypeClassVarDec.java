package types;

import ast.AstExp; // Ensure you import your Expression AST node

public class TypeClassVarDec extends Type {
    public Type t;
    public String name;
    public AstExp initExp; 

    public TypeClassVarDec(Type t, String name, AstExp initExp) {
        this.t = t;
        this.name = name;
        this.initExp = initExp;
    }
}