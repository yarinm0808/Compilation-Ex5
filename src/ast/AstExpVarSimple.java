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
    public int index; // To be retrieved from the Symbol Table
    public int offset;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstExpVarSimple(String name, int line, int offset) {
        this.serialNumber = AstNodeSerialNumber.getFresh();
        this.lineNumber = line;
        this.name = name;
        this.offset = offset;
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
        SymbolTableEntry entry = SymbolTable.getInstance().findEntry(name);

        if (entry == null) {
            throw new RuntimeException("ERROR(" + lineNumber + ")");
        }

        this.offset = entry.offset;

        return entry.type;
    }

    /***************************/
    /* IR for Simple Variable  */
    /***************************/
    @Override
    public Temp irMe() {
        Temp t = TempFactory.getInstance().getFreshTemp();
        
        if (this.offset != 0) {
            /**************************************************/
            /* LOCAL/PARAM: Name is null, use stack offset    */
            /**************************************************/
            Ir.getInstance().AddIrCommand(new IrCommandLoad(t, null, this.offset));
        } else {
            /**************************************************/
            /* GLOBAL: Use name, offset is 0                  */
            /**************************************************/
            Ir.getInstance().AddIrCommand(new IrCommandLoad(t, name, 0));
        }
        
        return t;
    }
}