/***********/
/* PACKAGE */
/***********/
package symboltable;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.io.PrintWriter;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import types.*;

/****************/
/* SYMBOL TABLE */
/****************/
public class SymbolTable
{
    private int hashArraySize = 13;

    /**********************************************/
    /* The actual symbol table data structure ... */
    /**********************************************/
    private SymbolTableEntry[] table = new SymbolTableEntry[hashArraySize];
    private SymbolTableEntry top;
    private int topIndex = 0;
    
    // Tracks the current nesting level: 0 = Global, 1+ = Inside Functions
    private int scopeLevel = 0; 
    
    public Type currentExpectedReturnType = null;

    /**************************************************************/
    /* A standard hash function for strings                       */
    /**************************************************************/
    private int hash(String s)
    {
        return Math.abs(s.hashCode()) % hashArraySize;
    }

    /**
     * Helper to check if we are currently in the global scope.
     */
    public boolean isGlobalScope() {
        return scopeLevel == 0;
    }

    /****************************************************************************/
    /* Enter a variable, function, class type or array type to the symbol table */
    /****************************************************************************/
    public SymbolTableEntry enter(String name, Type t)
    {
        int hashValue = hash(name);
        SymbolTableEntry next = table[hashValue];

        // We pass 'scopeLevel' to the entry so it knows if it's a global or local
        SymbolTableEntry e = new SymbolTableEntry(
            name, 
            t, 
            hashValue, 
            next, 
            top, 
            topIndex++, 
            0,
            scopeLevel 
        );
        
        top = e;
        table[hashValue] = e;

        return e; 
    }

    public Type findInCurrentScope(String name)
    {
        SymbolTableEntry e = top;
        while (e != null && !e.name.equals("SCOPE-BOUNDARY"))
        {
            if (e.name.equals(name))
            {
                return e.type;
            }
            e = e.prevtop;
        }
        return null;
    }

    public Type find(String name)
    {
        SymbolTableEntry e;
        for (e = table[hash(name)]; e != null; e = e.next)
        {
            if (name.equals(e.name))
            {
                return e.type;
            }
        }
        return null;
    }

    public SymbolTableEntry findEntry(String name)
    {
        SymbolTableEntry e;
        for (e = table[hash(name)]; e != null; e = e.next)
        {
            if (name.equals(e.name))
            {
                return e;
            }
        }
        return null;
    }

    /***************************************************************************/
    /* begin scope = Increment level and enter the <SCOPE-BOUNDARY>            */
    /***************************************************************************/
    public void beginScope()
    {
        scopeLevel++; 
        enter("SCOPE-BOUNDARY", new TypeForScopeBoundaries("NONE"));
    }

    /********************************************************************************/
    /* end scope = Pop elements and decrement level                                 */
    /********************************************************************************/
    public void endScope()
    {
        while (top != null && !top.name.equals("SCOPE-BOUNDARY"))
        {
            table[top.index] = top.next;
            topIndex = topIndex - 1;
            top = top.prevtop;
        }
        
        if (top != null) {
            table[top.index] = top.next;
            topIndex = topIndex - 1;
            top = top.prevtop;
        }
        
        scopeLevel--; // Return to outer scope level
    }

    public void printMe()
    {
        // Existing print logic...
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Current Symbol Table ---\n");
        SymbolTableEntry e = top;
        while (e != null) {
            sb.append(String.format("Name: %-15s | Type: %-10s | Scope: %d\n", 
                e.name, e.type.getClass().getSimpleName(), e.scopeLevel));
            e = e.prevtop;
        }
        sb.append("----------------------------");
        return sb.toString();
    }

    /**************************************/
    /* USUAL SINGLETON IMPLEMENTATION ... */
    /**************************************/
    private static SymbolTable instance = null;

    protected SymbolTable() {}

    public static SymbolTable getInstance()
    {
        if (instance == null)
        {
            instance = new SymbolTable();

            // Initialize Primitives
            instance.enter("int", TypeInt.getInstance());
            instance.enter("string", TypeString.getInstance());
            instance.enter("void", TypeVoid.getInstance());

            // Initialize Built-ins
            instance.enter("PrintInt", new TypeFunction(
                TypeVoid.getInstance(), "PrintInt", 
                new TypeList(TypeInt.getInstance(), null)));

            instance.enter("PrintString", new TypeFunction(
                TypeVoid.getInstance(), "PrintString", 
                new TypeList(TypeString.getInstance(), null)));
        }
        return instance;
    }
}