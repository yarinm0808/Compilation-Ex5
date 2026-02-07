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
    public Type currentExpectedReturnType = null;

    /**************************************************************/
    /* A standard hash function for strings                       */
    /**************************************************************/
    private int hash(String s)
    {
        // FIX: Use Java's built-in hashCode for better distribution
        // Math.abs handles potential negative hash codes
        return Math.abs(s.hashCode()) % hashArraySize;
    }

    /****************************************************************************/
    /* Enter a variable, function, class type or array type to the symbol table */
    /****************************************************************************/
    public void enter(String name, Type t)
    {
        /*************************************************/
        /* [1] Compute the hash value for this new entry */
        /*************************************************/
        int hashValue = hash(name);

        /******************************************************************************/
        /* [2] Extract what will eventually be the next entry in the hashed position  */
        /* NOTE: this entry can very well be null, but the behaviour is identical */
        /******************************************************************************/
        SymbolTableEntry next = table[hashValue];

        /**************************************************************************/
        /* [3] Prepare a new symbol table entry with name, type, next and prevtop */
        /**************************************************************************/
        // Use topIndex++ for the unique ID so y in main is 8 and global y is 0
        
        SymbolTableEntry e = new SymbolTableEntry(name, t, hashValue, next, top, topIndex++, 0);
        /**********************************************/
        /* [4] Update the top of the symbol table ... */
        /**********************************************/
        top = e;

        /****************************************/
        /* [5] Enter the new entry to the table */
        /****************************************/
        table[hashValue] = e;

        /**************************/
        /* [6] Print Symbol Table */
        /**************************/
        // printMe();
    }
    public Type findInCurrentScope(String name)
	{
    // Only look at the "top" scope, do not follow the chain downwards/upwards
    // If your table is a hash table of linked lists, we iterate the list 
    // until we hit "SCOPE-BOUNDARY".
    
    // NOTE: This implementation depends on your specific SymbolTable structure.
    // Assuming 'top' points to the most recent entry:
    
    SymbolTableEntry e = top;
    while (e != null && !e.name.equals("SCOPE-BOUNDARY"))
    {
        if (e.name.equals(name))
        {
            return e.type;
        }
        e = e.prevtop; // Move backward through the stack of recent entries
    }
    return null;
	}
    /***********************************************/
    /* Find the inner-most scope element with name */
    /***********************************************/
    public Type find(String name)
    {
        SymbolTableEntry e;

        // Iterate down the linked list for this hash bucket.
        // Since we insert at the HEAD (index 0), the first match we find
        // is the most recently added one (Inner Scope).
        for (e = table[hash(name)]; e != null; e = e.next)
        {
            if (name.equals(e.name))
            {
                return e.type;
            }
        }

        return null;
    }

    /***************************************************************************/
    /* begine scope = Enter the <SCOPE-BOUNDARY> element to the data structure */
    /***************************************************************************/
    public void beginScope()
    {
        /************************************************************************/
        /* Though <SCOPE-BOUNDARY> entries are present inside the symbol table, */
        /* they are not really types. In order to be able to debug print them,  */
        /* a special TYPE_FOR_SCOPE_BOUNDARIES was developed for them. This     */
        /* class only contain their type name which is the bottom sign: _|_     */
        /************************************************************************/
        enter(
            "SCOPE-BOUNDARY",
            new TypeForScopeBoundaries("NONE"));

        /*********************************************/
        /* Print the symbol table after every change */
        /*********************************************/
        // printMe();
    }

    /********************************************************************************/
    /* end scope = Keep popping elements out of the data structure,                 */
    /* from most recent element entered, until a <NEW-SCOPE> element is encountered */
    /********************************************************************************/
    public void endScope()
    {
        /**************************************************************************/
        /* Pop elements from the symbol table stack until a SCOPE-BOUNDARY is hit */
        /**************************************************************************/
        while (top != null && !top.name.equals("SCOPE-BOUNDARY"))
        {
            table[top.index] = top.next;
            topIndex = topIndex - 1;
            top = top.prevtop;
        }
        /**************************************/
        /* Pop the SCOPE-BOUNDARY sign itself */
        /**************************************/
        table[top.index] = top.next;
        topIndex = topIndex - 1;
        top = top.prevtop;

        /*********************************************/
        /* Print the symbol table after every change */
        /*********************************************/
        // printMe();
    }
    public SymbolTableEntry findEntry(String name)
    {
        SymbolTableEntry e;
        // Iterate down the linked list for this hash bucket.
        // The head of the list is the most recently added entry (inner scope).
        for (e = table[hash(name)]; e != null; e = e.next)
        {
            if (name.equals(e.name))
            {
                return e;
            }
        }
        return null;
    }

    public static int n = 0;

    public void printMe()
    {
        int i = 0;
        int j = 0;
        String dirname = "./output/";
        String filename = String.format("SYMBOL_TABLE_%d_IN_GRAPHVIZ_DOT_FORMAT.txt", n++);

        try
        {
            /*******************************************/
            /* [1] Open Graphviz text file for writing */
            /*******************************************/
            PrintWriter fileWriter = new PrintWriter(dirname + filename);

            /*********************************/
            /* [2] Write Graphviz dot prolog */
            /*********************************/
            fileWriter.print("digraph structs {\n");
            fileWriter.print("rankdir = LR\n");
            fileWriter.print("node [shape=record];\n");

            /*******************************/
            /* [3] Write Hash Table Itself */
            /*******************************/
            fileWriter.print("hashTable [label=\"");
            for (i = 0; i < hashArraySize - 1; i++) { fileWriter.format("<f%d>\n%d\n|", i, i); }
            fileWriter.format("<f%d>\n%d\n\"];\n", hashArraySize - 1, hashArraySize - 1);

            /****************************************************************************/
            /* [4] Loop over hash table array and print all linked lists per array cell */
            /****************************************************************************/
            for (i = 0; i < hashArraySize; i++)
            {
                if (table[i] != null)
                {
                    /*****************************************************/
                    /* [4a] Print hash table array[i] -> entry(i,0) edge */
                    /*****************************************************/
                    fileWriter.format("hashTable:f%d -> node_%d_0:f0;\n", i, i);
                }
                j = 0;
                for (SymbolTableEntry it = table[i]; it != null; it = it.next)
                {
                    /*******************************/
                    /* [4b] Print entry(i,it) node */
                    /*******************************/
                    fileWriter.format("node_%d_%d ", i, j);
                    fileWriter.format("[label=\"<f0>%s|<f1>%s|<f2>prevtop=%d|<f3>next\"];\n",
                        it.name,
                        it.type.name,
                        it.prevtopIndex);

                    if (it.next != null)
                    {
                        /***************************************************/
                        /* [4c] Print entry(i,it) -> entry(i,it.next) edge */
                        /***************************************************/
                        fileWriter.format(
                            "node_%d_%d -> node_%d_%d [style=invis,weight=10];\n",
                            i, j, i, j + 1);
                        fileWriter.format(
                            "node_%d_%d:f3 -> node_%d_%d:f0;\n",
                            i, j, i, j + 1);
                    }
                    j++;
                }
            }
            fileWriter.print("}\n");
            fileWriter.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**************************************/
    /* USUAL SINGLETON IMPLEMENTATION ... */
    /**************************************/
    private static SymbolTable instance = null;

    /*****************************/
    /* PREVENT INSTANTIATION ... */
    /*****************************/
    protected SymbolTable() {}

    /******************************/
    /* GET SINGLETON INSTANCE ... */
    /******************************/
    public static SymbolTable getInstance()
    {
        if (instance == null)
        {
            /*******************************/
            /* [0] The instance itself ... */
            /*******************************/
            instance = new SymbolTable();

            /*****************************************/
            /* [1] Enter primitive types int, string */
            /*****************************************/
            instance.enter("int", TypeInt.getInstance());
            instance.enter("string", TypeString.getInstance());

            /*************************************/
            /* [2] How should we handle void ??? */
            /*************************************/
            // FIX: Register void so 'void foo()' works.
            // NOTE: You must strictly check that variables are NOT declared as void 
            // in your AST declaration node.
            instance.enter("void", TypeVoid.getInstance());

            /***************************************/
            /* [3] Enter library function PrintInt */
            /***************************************/
            instance.enter(
                "PrintInt",
                new TypeFunction(
                    TypeVoid.getInstance(),
                    "PrintInt",
                    new TypeList(
                        TypeInt.getInstance(),
                        null)));

            /******************************************/
            /* [4] Enter library function PrintString */
            /******************************************/
            // FIX: Added per assignment requirements
            instance.enter(
                "PrintString",
                new TypeFunction(
                    TypeVoid.getInstance(),
                    "PrintString",
                    new TypeList(
                        TypeString.getInstance(),
                        null)));

        }
        return instance;
    }
}