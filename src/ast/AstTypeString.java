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

    public void printMe(){
        System.out.println(String.format("AST Type String"));
    }
    public String toString() { return "string"; }

    public String getTypeName(){
        return "String";
    }
}
