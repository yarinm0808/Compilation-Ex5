package ast;

import types.*;
public class AstTypeInt extends AstType{
    public AstTypeInt(){
        serialNumber = AstNodeSerialNumber.getFresh();
    }

    @Override
    public Type semantMe(){
        return TypeInt.getInstance();
    }

    public String toString() { return "int"; }
    public String getTypeName(){
        return "int";
    }
}
