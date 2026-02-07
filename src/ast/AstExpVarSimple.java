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
        // 1. Look up the variable in the Symbol Table
        // We use findEntry because we need the index, not just the Type.
        SymbolTableEntry entry = SymbolTable.getInstance().findEntry(name);

        // 2. Error if the variable was never declared
        if (entry == null) {
            throw new RuntimeException("ERROR(" + lineNumber + ")");
        }

        // 3. Capture the index so the IR knows which memory slot to read
        this.index = entry.prevtopIndex;

        // 4. Return the type of the variable (int, string, class, etc.)
        return entry.type;
    }

    /***************************/
    /* IR for Simple Variable  */
    /***************************/
	@Override
	public Temp irMe() {
		// 1. Get a unique temporary register from the singleton factory
		Temp t = TempFactory.getInstance().getFreshTemp();
		
		// 2. Load the variable value into that temp
		// Using 'name' here assumes your IR can resolve the variable's address by name
		Ir.getInstance().AddIrCommand(new IrCommandLoad(t, name, this.offset));
		
		// 3. Return the temp for the parent node to use
		return t;
	}
}