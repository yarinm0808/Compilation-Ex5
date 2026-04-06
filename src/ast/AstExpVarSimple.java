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
    protected SymbolTableEntry entry; 

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
        System.out.println(SymbolTable.getInstance().toString());
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
        int finalOffset;

        if (this.entry.scopeLevel == 0) {
            Ir.getInstance().AddIrCommand(new IrCommandLoad(t, name, 0));
            return t;
        }

        if (this.entry.isParameter) {
            finalOffset = this.entry.offset;
        } else {
            // -44 is the base for locals (after 40 bytes of regs + 8 bytes of FP/RA)
            finalOffset = -44 - (this.entry.offset * 4);
        }

        System.out.println("[DEBUG] Load Variable: " + name + " from offset: " + finalOffset);
        Ir.getInstance().AddIrCommand(new IrCommandLoad(t, null, finalOffset));
        return t;
    }
}