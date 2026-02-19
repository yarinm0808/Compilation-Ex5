package symboltable;

import types.*;

public class SymbolTableEntry
{
    public int index;
    public String name;
    public Type type;
    public SymbolTableEntry prevtop;
    public SymbolTableEntry next;
    public int prevtopIndex;
    public int lineNumber;
    public int offset; 

    public SymbolTableEntry(
        String name,
        Type type,
        int index,
        SymbolTableEntry next,
        SymbolTableEntry prevtop,
        int prevtopIndex,
        int lineNumber)
    {
        this.index = index;
        this.name = name;
        this.type = type;
        this.next = next;
        this.prevtop = prevtop;
        this.prevtopIndex = prevtopIndex;
        this.lineNumber = lineNumber;
    }

    /**************************************************/
    /* NEW: Method to set the offset during IR phase */
    /**************************************************/
    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getIndex() {
        return this.index;
    }
}