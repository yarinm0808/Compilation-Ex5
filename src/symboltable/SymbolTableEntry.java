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
    
    // NEW: Added scopeLevel to distinguish Global (0) from Local (>0)
    public int scopeLevel;
    public boolean isParameter; 

    public SymbolTableEntry(
        String name,
        Type type,
        int index,
        SymbolTableEntry next,
        SymbolTableEntry prevtop,
        int prevtopIndex,
        int lineNumber,
        int scopeLevel) // Added parameter here
    {
        this.index = index;
        this.name = name;
        this.type = type;
        this.next = next;
        this.prevtop = prevtop;
        this.prevtopIndex = prevtopIndex;
        this.lineNumber = lineNumber;
        this.scopeLevel = scopeLevel; // Initialize the level
        this.isParameter = false;
    }

    public void markAsParameter() {
        this.isParameter = true;
    }
    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getIndex() {
        return this.index;
    }

    // NEW: Helper to check if global
    public boolean isGlobal() {
        return scopeLevel == 0;
    }
}