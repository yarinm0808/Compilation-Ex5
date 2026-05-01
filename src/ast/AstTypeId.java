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
            throw new RuntimeException("ERROR(" + this.lineNumber + ")");
        }
        return t;
    }

    public void printMe(){
        System.out.println(String.format("AST Type Id with name: %s", this.name));
    }

    @Override
    public String toString() { return this.name; }

    public String getTypeName(){
        return this.name;
    };
    
}
