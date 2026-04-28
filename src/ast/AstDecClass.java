package ast;

import types.*;
import symboltable.*;
import temp.Temp;

public class AstDecClass extends AstDec
{
    public String name;
    public String father;
    public AstDecList dataMembers; 

    // Constructor for class with fields
    public AstDecClass(String name, AstDecList dataMembers)
    {
        serialNumber = AstNodeSerialNumber.getFresh();
        this.name = name;
        this.dataMembers = dataMembers;
        this.father = null;
    }

    // Constructor for inheritance 
    public AstDecClass(String name, String father, AstDecList dataMembers)
    {
        serialNumber = AstNodeSerialNumber.getFresh();
        this.name = name;
        this.father = father;
        this.dataMembers = dataMembers;
    }

    public void printMe()
    {
        System.out.format("CLASS DEC = %s\n", name);
        if (dataMembers != null) dataMembers.printMe();
        
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("CLASS\n%s", name));
        
        if (dataMembers != null) {
            AstGraphviz.getInstance().logEdge(serialNumber, dataMembers.serialNumber);
        }
    }

    public Type semantMe() {
        // 1. Resolve Father Class
        TypeClass fatherType = null;
        if (this.father != null) {
            Type t = SymbolTable.getInstance().find(this.father);
            if (t instanceof TypeClass) {
                fatherType = (TypeClass) t;
            } else {
                throw new RuntimeException("ERROR(" + lineNumber + ")");
            }
        }
        
        TypeClass tc = new TypeClass(fatherType, name, null);
        SymbolTable.getInstance().enter(name, tc);

        SymbolTable.getInstance().setInsideClass(true);
        SymbolTable.getInstance().beginScope();

        // ==========================================
        // THE DEEP INHERITANCE FIX
        // ==========================================
        
        // 1. Build a list of all ancestors (climbing the tree)
        java.util.List<TypeClass> ancestors = new java.util.ArrayList<>();
        TypeClass curr = fatherType;
        while (curr != null) {
            ancestors.add(curr);
            // Note: If your TypeClass uses the field name 'father' instead of 'parent', 
            // change the line below to: curr = curr.father;
            curr = curr.father; 
        }
        
        // 2. Reverse the list so we process Top-Down (A -> B -> C)
        // This ensures proper shadowing and offset alignment.
        java.util.Collections.reverse(ancestors);
        
        int currentHeapOffset = 4; // Start right after VMT
        
        // 3. Import all fields and methods into the current scope
        for (TypeClass anc : ancestors) {
            for (TypeList it = anc.data_members; it != null; it = it.tail) {
                if (it.head instanceof TypeClassVarDec) {
                    TypeClassVarDec vd = (TypeClassVarDec) it.head;
                    SymbolTable.getInstance().enter(vd.name, vd.t);
                    
                    // CRITICAL: Ensure inherited fields get the isField flag AND correct offset
                    SymbolTableEntry entry = SymbolTable.getInstance().findEntry(vd.name);
                    entry.isField = true;
                    entry.setOffset(currentHeapOffset);
                    
                    currentHeapOffset += 4; // Accumulate offset for the next field
                } else if (it.head instanceof TypeFunction) {
                    TypeFunction tf = (TypeFunction) it.head;
                    SymbolTable.getInstance().enter(tf.name, tf);
                }
            }
        }

        // ==========================================

        TypeList membersHead = null;
        TypeList membersTail = null;

        // --- PASS 1: Fields ---
        // currentHeapOffset is now perfectly aligned right after the last ancestor field!
        for (AstDecList it = this.dataMembers; it != null; it = it.tail) {
            if (it.head instanceof AstDecVar) {
                AstDecVar varDec = (AstDecVar) it.head;
                Type fieldType = varDec.type.semantMe();
                
                SymbolTable.getInstance().enter(varDec.name, fieldType);
                SymbolTableEntry entry = SymbolTable.getInstance().findEntry(varDec.name);
                entry.isField = true;
                entry.setOffset(currentHeapOffset); 
                
                TypeClassVarDec vd = new TypeClassVarDec(fieldType, varDec.name, varDec.initialValue);
                
                // APPEND to list
                TypeList newNode = new TypeList(vd, null);
                if (membersHead == null) { membersHead = newNode; membersTail = newNode; }
                else { membersTail.tail = newNode; membersTail = newNode; }
                
                currentHeapOffset += 4;
            }
        }

        // --- PASS 2: Methods ---
        for (AstDecList it = this.dataMembers; it != null; it = it.tail) {
            if (it.head instanceof AstDecFunc) {
                AstDecFunc funcDec = (AstDecFunc) it.head;
                funcDec.className = this.name; 
                Type methodType = funcDec.semantMe(); 
                
                if (methodType instanceof TypeFunction) {
                    ((TypeFunction) methodType).definingClassName = this.name;
                }
                
                TypeList newNode = new TypeList(methodType, null);
                if (membersHead == null) { membersHead = newNode; membersTail = newNode; }
                else { membersTail.tail = newNode; membersTail = newNode; }
            }
        }

        SymbolTable.getInstance().endScope();
        SymbolTable.getInstance().setInsideClass(false);
        tc.data_members = membersHead; 
        return tc;
    }

    @Override
    public Temp irMe() {
        for (AstDecList it = this.dataMembers; it != null; it = it.tail) {
            if (it.head instanceof AstDecFunc) {
                it.head.irMe(); 
            }
        }
        return null; 
    }
}