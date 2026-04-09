package ast;

import types.*;
public class AstTypeVoid extends AstType {
    public AstTypeVoid(){
        serialNumber = AstNodeSerialNumber.getFresh();
    }

    @Override
    public Type semantMe(){
        return TypeVoid.getInstance();
    }

    public void printMe(){
        System.out.println(String.format("AST Type Void"));
    }

    public String toString() { return "void"; }

    public String getTypeName(){
        return "void";
    };
}
