package ast;

import types.*;
import symboltable.*;

public class AstTypeId extends AstType{
    public String name;
    public AstTypeId(String name){
        this.name = name;
        serialNumber = AstNodeSerialNumber.getFresh();
    }

    @Override
    public Type semantMe(){
        Type t = SymbolTable.getInstance().find(this.name);
        if(t == null){
            throw new RuntimeException(String.format("ERROR"));
        }
        return t;
    }
    
}
