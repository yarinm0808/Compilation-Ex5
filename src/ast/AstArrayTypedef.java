package ast;

import types.*;
import symboltable.SymbolTable;

public class AstArrayTypedef extends AstDec {
    public String arrayName;
    public AstType baseType;

    public AstArrayTypedef(String name, AstType type, int line) {
        this.serialNumber = AstNodeSerialNumber.getFresh();
        this.lineNumber = line;
        this.arrayName = name;
        this.baseType = type;
    }

    @Override
    public Type semantMe() {
        // 1. Get the actual Type object for the base (e.g., TypeInt)
        Type t = baseType.semantMe();

        // 2. Create a new Array Type definition
        TypeArray arrayType = new TypeArray(t, arrayName);

        // 3. Register this name in the Symbol Table
        // Check if the name is already taken first!
        if (SymbolTable.getInstance().findInCurrentScope(arrayName) != null) {
            throw new RuntimeException("ERROR(" + lineNumber + ")");
        }
        
        SymbolTable.getInstance().enter(arrayName, arrayType);

        return arrayType;
    }
}