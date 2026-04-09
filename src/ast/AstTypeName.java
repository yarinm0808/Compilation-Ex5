package ast;

import types.*;
import symboltable.*;

public class AstTypeName extends AstNode {
    // CHANGE: Use the node itself, not just its name string
    public AstType typeNode; 
    public String name;
    public SymbolTableEntry entry; 

    public AstTypeName(AstType typeNode, String name) {
        this.serialNumber = AstNodeSerialNumber.getFresh();
        this.typeNode = typeNode;
        this.name = name;
    }

    @Override
    public void printMe() {
        // Use the node's toString or a placeholder for printing
        System.out.format("NAME(%s):TYPE(%s)\n", name, typeNode.getClass().getSimpleName());
    }

    @Override
    public Type semantMe() {
        // Just resolve the type, DO NOT call enter()
        Type t = typeNode.semantMe();
        
        if (t == null) {
            throw new RuntimeException("ERROR(" + lineNumber + "): non existing type");
        }
        
        // We return the type so the caller (like AstDecFunc) can use it
        return t;
    }
}