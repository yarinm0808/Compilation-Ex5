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

    @Override
    public Temp irMe() {
        Temp t = TempFactory.getInstance().getFreshTemp();

        // CASE 1: Class Fields (Implicit 'this' access)
        // Used when you access a field like 'age' from within a class method.
        if (this.entry.isField) {
            Temp tThis = TempFactory.getInstance().getFreshTemp();
            
            // 1. Load the 'this' pointer from the standard hidden parameter slot [cite: 98]
            // In the TAU architecture, 'this' is the first parameter at 8($fp)
            Ir.getInstance().AddIrCommand(new IrCommandLoad(tThis, null, 8));
            
            // 2. Load the actual field from the heap: lw $t, offset($tThis) [cite: 73]
            Ir.getInstance().AddIrCommand(new IrCommandLoadField(t, tThis, this.entry.offset));
            return t;
        }

        // CASE 2: Global Variables
        // These live in the .data section and are accessed via labels. [cite: 87]
        if (this.entry.scopeLevel == 0) {
            // Generates: lw $t, labelName
            Ir.getInstance().AddIrCommand(new IrCommandLoad(t, this.entry.name, 0));
            return t;
        }

        // CASE 3: Locals and Parameters
        // These live on the stack relative to the Frame Pointer ($fp). [cite: 93]
        int finalOffset;
        if (this.entry.isParameter) {
            // IMPORTANT: Use the raw offset from semantMe (8, 12, 16...)
            // Do NOT multiply by 4 again here; it's already a byte offset!
            finalOffset = this.entry.offset;
        } else {
            // Locals start AFTER the 10 saved registers ($t0-$t9)
            // Index 0 -> -44($fp), Index 1 -> -48($fp), etc.
            finalOffset = -44 - (this.entry.offset * 4);
        }

        Ir.getInstance().AddIrCommand(new IrCommandLoad(t, null, finalOffset));
        return t;
    }
}