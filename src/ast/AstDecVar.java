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

        // 2. Collision Check in current scope
        if (SymbolTable.getInstance().findInCurrentScope(name) != null) {
            throw new RuntimeException("ERROR(" + lineNumber + "): variable " + name + " already defined in scope.");
        }

        // 3. Register in Symbol Table and capture the entry
        // This entry now contains the scopeLevel (0 for global, >0 for local)
        this.entry = SymbolTable.getInstance().enter(name, t);

        // 4. Initial Value Semantics
        if (initialValue != null) {
            Type initType = initialValue.semantMe();
            // Optional: Add type compatibility check here (initType vs t)
        }

        return t;
    }

    /***************************/
    /* IR for Var Declaration  */
    /***************************/
    @Override
    public Temp irMe() {
        if (this.entry.scopeLevel == 0) {
            System.out.println("[DEBUG] Global Var Detected: " + name);
            // GLOBAL VARIABLE
            if (type.semantMe() instanceof TypeString && initialValue instanceof AstExpString) {
                System.out.println("[DEBUG] Creating IrCommandAllocateString for: " + name);
                String strVal = ((AstExpString)initialValue).value;
                // Add a command that handles the dual .data allocation
                Ir.getInstance().AddIrCommand(new IrCommandAllocateString(name, strVal));
            } else {
                // Standard Global Allocation
                System.out.println("[DEBUG] Creating IrCommandAllocate for: " + name);
                Ir.getInstance().AddIrCommand(new IrCommandAllocate(name));
            }
        } else {
            // LOCAL VARIABLE
            int localOffset = StackOffsetManager.getInstance().getNextOffset();
            this.entry.setOffset(localOffset);
        }

        // Handle Assignment/Initialization logic
        if (initialValue != null) {
            // For simple strings, the allocation might already handle the value.
            // For complex initializations (e.g., string z = someFunc();), execute standard IR:
            Temp val = initialValue.irMe();
            if (this.entry.scopeLevel > 0) {
                Ir.getInstance().AddIrCommand(new IrCommandStore(null, val, this.entry.offset));
            } else {
                Ir.getInstance().AddIrCommand(new IrCommandStore(name, val, 0));
            }
        }
        return null;
    }
}