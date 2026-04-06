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
        // CHANGE: Delegate the lookup to the type node
        Type t = typeNode.semantMe();
        
        if (t == null) {
            System.out.format(">> ERROR: non existing type\n");
            // Instead of exit(0), usually return TypeInt as a fallback
            return TypeInt.getInstance(); 
        }
        
        this.entry = SymbolTable.getInstance().enter(name, t);
        return t;
    }
}