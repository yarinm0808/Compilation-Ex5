/***********/
/* PACKAGE */
/***********/
package ast;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import types.*;
import symboltable.*;
import temp.*;
import ir.*;

public class AstDecVar extends AstDec {
    /****************/
    /* DATA MEMBERS */
    /****************/
    public AstType type;
    public String name;
    public AstExp initialValue;
    public int index; // Captured from SymbolTable for IR/MIPS use

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstDecVar(AstType type, String name, AstExp initialValue, int line) {
        this.serialNumber = AstNodeSerialNumber.getFresh();
        this.lineNumber = line;
        this.type = type;
        this.name = name;
        this.initialValue = initialValue;
    }

    /************************************************************/
    /* The printing message for a variable declaration AST node */
    /************************************************************/
    @Override
    public void printMe() {
        System.out.format("VAR-DEC(%s)\n", name);
        if (type != null) type.printMe();
        if (initialValue != null) initialValue.printMe();

        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("VAR\nDEC(%s)", name));

        if (type != null) AstGraphviz.getInstance().logEdge(serialNumber, type.serialNumber);
        if (initialValue != null) AstGraphviz.getInstance().logEdge(serialNumber, initialValue.serialNumber);
    }

    /*********************************/
    /* SEMANTICS for Var Declaration */
    /*********************************/
    @Override
    public Type semantMe() {
        // 1. Resolve the type (int, string, or class ID)
        Type t = type.semantMe();

        // 2. Collision Check: Ensure not already defined in the CURRENT scope
        if (SymbolTable.getInstance().findInCurrentScope(name) != null) {
            throw new RuntimeException("ERROR(" + lineNumber + ")");
        }

        // 3. Initial Value Semantics (Check before entering to prevent recursive use)
        if (initialValue != null) {
            Type initType = initialValue.semantMe();
            // Note: Add type compatibility checks here if needed (e.g., initType == t)
        }

        // 4. Register in Symbol Table
        SymbolTable.getInstance().enter(name, t);

        // 5. Capture the unique index for IR generation
        SymbolTableEntry entry = SymbolTable.getInstance().findEntry(name);
        if (entry != null) {
            this.index = entry.prevtopIndex;
        }

        return t;
    }

    /***************************/
    /* IR for Var Declaration  */
    /***************************/
    @Override
    public Temp irMe() {
        // Handle allocation (often a no-op if using stack offsets, but keep for consistency)
        Ir.getInstance().AddIrCommand(new IrCommandAllocate(name));

        if (initialValue != null) {
            Temp val = initialValue.irMe();
            Ir.getInstance().AddIrCommand(new IrCommandStore(name, val));
        }
        return null;
    }
}