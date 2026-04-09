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
        // 1. Resolve the type node (e.g., 'int' or 'Person')
        Type t = type.semantMe();
        
        // 2. COLLISION CHECK: Ensure we aren't re-declaring in the same scope
        if (SymbolTable.getInstance().findInCurrentScope(name) != null) {
            throw new RuntimeException("ERROR(" + lineNumber + "): variable " + name + " already defined.");
        }

        // 3. Register in Symbol Table
        // The entry captures the name and type, and we'll tag it for IR below
        this.entry = SymbolTable.getInstance().enter(name, t);

        // 4. COORDINATE ALLOCATION (The MIPS Handshake)
        int level = SymbolTable.getInstance().getScopeLevel();
        boolean insideClass = SymbolTable.getInstance().isInsideClass();

        if (level == 0) {
            // GLOBAL SCOPE
            this.entry.isField = false;
            this.entry.isParameter = false;
        } 
        else if (insideClass && level == 1) {
            // CLASS FIELD SCOPE
            // Marked as field so IR knows to use the 'this' pointer + heap offset
            this.entry.isField = true;
            this.entry.isParameter = false;
            // Note: Specific heap offsets (0, 4, 8) are set by AstDecClass's loop
        } 
        else {
            // LOCAL SCOPE (Inside a Function, Method, If, or While)
            // Marked as local so IR knows to use the stack (-44, -48...)
            this.entry.isField = false;
            this.entry.isParameter = false;
            
            // Claim a unique index for this function's stack frame
            int uniqueIndex = SymbolTable.getInstance().allocateLocalVarIndex();
            this.entry.setOffset(uniqueIndex);
        }

        // 5. Initial Value Semantics (moish.age := 10)
        if (initialValue != null) {
            Type initType = initialValue.semantMe();
            
            // Optional: Check if initType is compatible with t
            // if (!t.isCompatibleWith(initType)) { ... error ... }
        }

        return t;
    }

    /***************************/
    /* IR for Var Declaration  */
    /***************************/
    @Override
    public Temp irMe() {
        /****************************************************************/
        /* 1. RE-REGISTER in the Symbol Table                           */
        /* This is CRITICAL. Since the Semantic Pass cleared the table, */
        /* we must put the variable back so IR nodes can find it.      */
        /****************************************************************/
        SymbolTable.getInstance().enter(name, this.entry.type);

        /****************************************************************/
        /* 2. HANDLE ALLOCATION (GLOBAL VS LOCAL)                       */
        /****************************************************************/
        if (this.entry.scopeLevel == 0) {
            // --- GLOBAL VARIABLE ---
            // Allocation in the .data segment
            if (this.entry.type instanceof TypeString && initialValue instanceof AstExpString) {
                String strVal = ((AstExpString) initialValue).value;
                Ir.getInstance().AddIrCommand(new IrCommandAllocateString(name, strVal));
            } else {
                Ir.getInstance().AddIrCommand(new IrCommandAllocate(name));
            }
        } else {
            // --- LOCAL VARIABLE ---
            // 1. Get the raw index from your offset manager
            int localIndex = StackOffsetManager.getInstance().getNextOffset();
            
            // 2. Save it back into the entry so we can find it later
            this.entry.setOffset(localIndex);
            this.entry.isParameter = false; 
        }

        /****************************************************************/
        /* 3. HANDLE INITIALIZATION (ASSIGNMENT)                        */
        /****************************************************************/
        if (initialValue != null) {
            // Skip re-storing global strings (they are initialized at allocation)
            if (this.entry.scopeLevel == 0 && 
                this.entry.type instanceof TypeString && 
                initialValue instanceof AstExpString) {
                return null; 
            }

            // Generate IR for the right-hand side expression
            Temp val = initialValue.irMe();
            
            if (this.entry.scopeLevel > 0) {
                // Calculate the actual MIPS stack offset (e.g., -44, -48...)
                // Note: Ensure this formula matches your stack frame layout!
                int finalStackOffset = -44 - (this.entry.offset * 4);
                
                // Generate a STORE command to the stack slot
                Ir.getInstance().AddIrCommand(new IrCommandStore(null, val, finalStackOffset));
            } else {
                // Global store: use the variable name as the label
                Ir.getInstance().AddIrCommand(new IrCommandStore(name, val, 0));
            }
        }

        return null;
    }
}