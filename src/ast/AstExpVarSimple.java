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

public class AstExpVarSimple extends AstExpVar {
    /****************/
    /* DATA MEMBERS */
    /****************/
    public String name;
    
    /** * This field is the "bridge." It is filled during semantMe() 
     * and used during irMe(). This prevents the "Symbol disappeared" 
     * error when the Symbol Table clears at the end of a scope.
     */
    private SymbolTableEntry entry; 

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstExpVarSimple(String name, int line, int column) {
        this.serialNumber = AstNodeSerialNumber.getFresh();
        this.lineNumber = line;
        this.name = name;
    }

    /***********************************************************/
    /* The printing message for a simple variable AST node     */
    /***********************************************************/
    @Override
    public void printMe() {
        System.out.format("VAR-SIMPLE(%s)\n", name);

        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("VAR\nSIMPLE(%s)", name));
    }

    /*********************************/
    /* SEMANTICS for Simple Variable */
    /*********************************/
    @Override
    public Type semantMe() {
        // [1] Find the entry in the Symbol Table
        this.entry = SymbolTable.getInstance().findEntry(name);

        if (this.entry == null) {
            throw new RuntimeException("ERROR(" + lineNumber + "): Variable " + name + " is not defined.");
        }

        // [2] Return the type found in the entry
        return this.entry.type;
    }

    /***************************/
    /* IR for Simple Variable  */
    /***************************/
    @Override
    public Temp irMe() {
        Temp t = TempFactory.getInstance().getFreshTemp();
        
        // [3] Use the PERSISTENT entry saved during semantics.
        // We do NOT call SymbolTable.findEntry here because the 
        // local scope may have already been closed.
        if (this.entry == null) {
            throw new RuntimeException("Internal Error: Variable " + name + " has no linked SymbolTableEntry.");
        }

        // [4] Check if it's Global (Level 0) or Local/Param (Level > 0)
        if (this.entry.scopeLevel > 0) {
            /**************************************************/
            /* LOCAL/PARAM: Use stack offset                  */
            /**************************************************/
            // MIPS logic: lw $tX, offset($sp)
            Ir.getInstance().AddIrCommand(new IrCommandLoad(t, null, this.entry.offset));
        } else {
            /**************************************************/
            /* GLOBAL: Use name                               */
            /**************************************************/
            // MIPS logic: lw $tX, global_name
            Ir.getInstance().AddIrCommand(new IrCommandLoad(t, name, 0));
        }
        
        return t;
    }
}