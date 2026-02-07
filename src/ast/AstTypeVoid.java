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
}
