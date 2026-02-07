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
    
}
