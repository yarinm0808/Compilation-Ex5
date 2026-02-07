package ast;
import types.*;

public class AstTypeString extends AstType {
    public AstTypeString(){
        serialNumber = AstNodeSerialNumber.getFresh();
    }    

    @Override
    public Type semantMe(){
        return TypeString.getInstance();
    }
}
