package ast;

import types.*;
import symboltable.*;
import temp.Temp;

public class AstDecClass extends AstDec
{
    public String name;
    public String father;
    public AstDecList dataMembers; // Changed from AstTypeNameList

    // Constructor for class with fields
    public AstDecClass(String name, AstDecList dataMembers)
    {
        serialNumber = AstNodeSerialNumber.getFresh();
        this.name = name;
        this.dataMembers = dataMembers;
        this.father = null;
    }

    // Constructor for inheritance (if you use it)
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
        
        // 1. Recursive print
        if (dataMembers != null) dataMembers.printMe();
        
        // 2. Log this node
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("CLASS\n%s", name));
        
        // 3. Log edge ONLY if dataMembers isn't null
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
                throw new RuntimeException("ERROR(" + lineNumber + "): superclass " + this.father + " not found.");
            }
        }
        
        // Register the class so it can reference itself
        TypeClass tc = new TypeClass(fatherType, name, null);
        SymbolTable.getInstance().enter(name, tc);

        // 2. Open Class Scope
        SymbolTable.getInstance().setInsideClass(true);
        SymbolTable.getInstance().beginScope();

        // 3. Import Father's members
        if (fatherType != null) {
            for (TypeList it = fatherType.data_members; it != null; it = it.tail) {
                if (it.head instanceof TypeClassVarDec) {
                    TypeClassVarDec vd = (TypeClassVarDec) it.head;
                    SymbolTable.getInstance().enter(vd.name, vd.t);
                } else if (it.head instanceof TypeFunction) {
                    TypeFunction tf = (TypeFunction) it.head;
                    SymbolTable.getInstance().enter(tf.name, tf);
                }
            }
        }

        TypeList membersList = null;

        // --- PASS 1: Fields (Tagging for IR) ---
        // Start after the father's memory footprint
        int currentHeapOffset = (fatherType != null) ? fatherType.getFieldCount() * 4 : 0;

        for (AstDecList it = this.dataMembers; it != null; it = it.tail) {
            if (it.head instanceof AstDecVar) {
                AstDecVar varDec = (AstDecVar) it.head;
                Type fieldType = varDec.type.semantMe();
                
                SymbolTable.getInstance().enter(varDec.name, fieldType);
                
                SymbolTableEntry entry = SymbolTable.getInstance().findEntry(varDec.name);
                entry.isField = true;
                entry.setOffset(currentHeapOffset);
                
                // --- UPDATED LINE BELOW ---
                // Pass varDec.initialValue as the third argument
                TypeClassVarDec vd = new TypeClassVarDec(fieldType, varDec.name, varDec.initialValue);
                
                membersList = new TypeList(vd, membersList);
                currentHeapOffset += 4;
            }
        }

        // --- PASS 2: Analyze Method Bodies & Mangle Labels ---
        for (AstDecList it = this.dataMembers; it != null; it = it.tail) {
            if (it.head instanceof AstDecFunc) {
                AstDecFunc funcDec = (AstDecFunc) it.head;
                
                // Tag the node so AstDecFunc.irMe generates 'Student_getAverage'
                funcDec.className = this.name; 
                
                Type methodType = funcDec.semantMe(); 
                
                // NEW: Tag the Type metadata so callers know where to jump
                if (methodType instanceof TypeFunction) {
                    ((TypeFunction) methodType).definingClassName = this.name;
                }
                
                membersList = new TypeList(methodType, membersList);
            }
        }

        SymbolTable.getInstance().endScope();
        SymbolTable.getInstance().setInsideClass(false);
        tc.data_members = membersList;
        return tc;
    }

    @Override
    public Temp irMe() {
        // We don't allocate the class here (that's the 'new' operator's job)
        // We just need to traverse into the methods to generate their MIPS bodies.
        for (AstDecList it = this.dataMembers; it != null; it = it.tail) {
            if (it.head instanceof AstDecFunc) {
                // This call triggers AstDecFunc.irMe(), which writes the 
                // 'Student_getAverage:' block into your IR/MIPS file.
                it.head.irMe(); 
            }
        }
        return null; 
    }
}