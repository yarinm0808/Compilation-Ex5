package ast;

import temp.Temp;
import temp.TempFactory;
import types.*;
import ir.*;

public class AstExpVarField extends AstExpVar {
    public AstExpVar var;
    public String fieldName;

    /**
     * THE BRIDGE:
     * This stores the class metadata we found during the Semantic phase.
     * We use it during the IR phase to find offsets without re-searching the Symbol Table.
     */
    protected TypeClass cachedBaseClassType;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstExpVarField(AstExpVar var, String fieldName) {
        serialNumber = AstNodeSerialNumber.getFresh();
        this.var = var;
        this.fieldName = fieldName;
    }

    @Override
    public void printMe() {
        System.out.format("FIELD\nNAME\n(___.%s)\n", fieldName);
        if (var != null) var.printMe();

        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("FIELD\nVAR\n___.%s", fieldName));

        if (var != null) AstGraphviz.getInstance().logEdge(serialNumber, var.serialNumber);
    }

    /*********************************/
    /* SEMANTICS: The Research Phase */
    /*********************************/
    @Override
    public Type semantMe() {
        // 1. Resolve the base (e.g., 'l1' in 'l1.head')
        Type baseType = var.semantMe();

        // 2. Validate that we are accessing a field of a Class
        if (!(baseType instanceof TypeClass)) {
            throw new RuntimeException("ERROR(" + lineNumber + "): Attempting field access on non-class type.");
        }

        // 3. LOCK IN THE RESULT: Save the class metadata for the IR phase
        this.cachedBaseClassType = (TypeClass) baseType;

        // 4. Verify the field exists and return its type
        Type fieldType = cachedBaseClassType.findFieldType(fieldName);
        if (fieldType == null) {
            throw new RuntimeException("ERROR(" + lineNumber + "): Field '" + fieldName + "' not found in class.");
        }

        return fieldType;
    }

    /*********************************/
    /* IR: The Construction Phase   */
    /*********************************/
    @Override
    public Temp irMe() {
        // [1] Get the base address (Generate the 'l1' pointer)
        // This calls var.irMe(), which uses the entry you cached in AstExpVarSimple
        Temp baseAddr = var.irMe();

        // [2] Safety: Check for Null Pointer
        Ir.getInstance().AddIrCommand(new IrCommand_Check_Null_Ptr(baseAddr));

        // [3] Use our cached bridge to find the field's offset
        // We NO LONGER call var.semantMe() here!
        int offset = cachedBaseClassType.findFieldOffset(fieldName);

        // [4] Create destination Temp
        Temp result = TempFactory.getInstance().getFreshTemp();

        // [5] Load the field value from the heap
        Ir.getInstance().AddIrCommand(new IrCommandLoadField(result, baseAddr, offset));

        return result;
    }
}