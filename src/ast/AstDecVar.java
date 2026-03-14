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
            // --- GLOBAL VARIABLE ---
            if (type.semantMe() instanceof TypeString && initialValue instanceof AstExpString) {
                String strVal = ((AstExpString)initialValue).value;
                Ir.getInstance().AddIrCommand(new IrCommandAllocateString(name, strVal));
            } else {
                Ir.getInstance().AddIrCommand(new IrCommandAllocate(name));
            }
        } else {
            // --- LOCAL VARIABLE ---
            // 1. Get the raw index (0, 1, 2...) from the manager
            int localIndex = StackOffsetManager.getInstance().getNextOffset();
            this.entry.setOffset(localIndex);
            
            // 2. Mark it as NOT a parameter explicitly (safety)
            this.entry.isParameter = false; 
        }

        // Handle Assignment/Initialization logic
        if (initialValue != null) {
            // If it's a global string literal, we already handled allocation/init.
            // For locals or global ints, we need to generate the Store command.
            if (this.entry.scopeLevel == 0 && type.semantMe() instanceof TypeString && initialValue instanceof AstExpString) {
                return null; 
            }

            Temp val = initialValue.irMe();
            
            if (this.entry.scopeLevel > 0) {
                // Calculate the actual MIPS stack offset: -44, -48, -52...
                int finalStackOffset = -44 - (this.entry.offset * 4);
                
                // Store the value into the local stack slot
                Ir.getInstance().AddIrCommand(new IrCommandStore(null, val, finalStackOffset));
            } else {
                // Global store (using variable name label)
                Ir.getInstance().AddIrCommand(new IrCommandStore(name, val, 0));
            }
        }
        return null;
    }
}