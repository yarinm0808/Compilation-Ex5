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
    public SymbolTableEntry entry;

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
        // 1. Resolve the type
        Type t = type.semantMe();

        // 2. Collision Check
        if (SymbolTable.getInstance().findInCurrentScope(name) != null) {
            throw new RuntimeException("ERROR(" + lineNumber + ")");
        }

        // 3. Initial Value Semantics
        if (initialValue != null) {
            initialValue.semantMe();
        }

        // 4. Register in Symbol Table and capture the entry ONCE
        // This 'entry' now holds the 'offset' we might set later
        this.entry = SymbolTable.getInstance().enter(name, t);

        return t;
    }

    /***************************/
    /* IR for Var Declaration  */
    /***************************/
    @Override
    public Temp irMe() {
        // 1. Check if it's Global (offset 0) or Local/Param (offset != 0)
        if (entry.offset == 0) {
            // Allocate in the MIPS .data segment
            Ir.getInstance().AddIrCommand(new IrCommandAllocate(name));
        }

        // 2. Handle Initialization (e.g., int x = 5;)
        if (initialValue != null) {
            Temp val = initialValue.irMe();
            
            if (entry.offset != 0) {
                // LOCAL: MIPS will generate sw $t, offset($sp)
                Ir.getInstance().AddIrCommand(new IrCommandStore(null, val, entry.offset));
            } else {
                // GLOBAL: MIPS will generate sw $t, labelName
                Ir.getInstance().AddIrCommand(new IrCommandStore(name, val, 0));
            }
        }
        return null;
    }
}