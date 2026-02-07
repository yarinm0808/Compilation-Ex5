package ast;

import types.*;
import symboltable.*;
import temp.*;
import ir.*;

public class AstNewArray extends AstExp {
    public AstType type;
    public AstExp size;

    public AstNewArray(AstType type, AstExp size, int line) {
        this.serialNumber = AstNodeSerialNumber.getFresh();
        this.lineNumber = line;
        this.type = type;
        this.size = size;
    }

    @Override
    public Type semantMe() {
        // 1. Resolve the element type (e.g., int)
        Type t = type.semantMe();

        // 2. Ensure the size expression is an integer
        Type sizeType = size.semantMe();
        if (!(sizeType instanceof TypeInt)) {
            throw new RuntimeException("ERROR(" + lineNumber + ")");
        }

        // 3. Return a TypeArray (this tells the parent node this expression results in an array)
        return new TypeArray(t, null); 
    }

    @Override
    public Temp irMe() {
        // 1. Evaluate the size expression to get a Temp
        Temp sizeTemp = size.irMe();

        // 2. Prepare a Temp to hold the resulting pointer
        Temp dst = TempFactory.getInstance().getFreshTemp();

        // 3. Generate the IR Command to allocate memory
        // This command should handle adding +1 for the length header
        Ir.getInstance().AddIrCommand(new IrCommandNewArray(dst, sizeTemp));

        return dst;
    }
}