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
                throw new RuntimeException("ERROR(" + lineNumber + ")");
            }
        }
        
        TypeClass tc = new TypeClass(fatherType, name, null);
        SymbolTable.getInstance().enter(name, tc);

        SymbolTable.getInstance().setInsideClass(true);
        SymbolTable.getInstance().beginScope();

        // Import Father's members into current scope
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

        // We use a Head and Tail pointer to APPEND to the list (keeping order)
        TypeList membersHead = null;
        TypeList membersTail = null;

        // --- PASS 1: Fields ---
        // Start at 4 (after VMT) + (Father's fields)
        int currentHeapOffset = 4; 
        if (fatherType != null) {
            currentHeapOffset += (fatherType.getFieldCount() * 4);
        }

        for (AstDecList it = this.dataMembers; it != null; it = it.tail) {
            if (it.head instanceof AstDecVar) {
                AstDecVar varDec = (AstDecVar) it.head;
                Type fieldType = varDec.type.semantMe();
                
                SymbolTable.getInstance().enter(varDec.name, fieldType);
                SymbolTableEntry entry = SymbolTable.getInstance().findEntry(varDec.name);
                entry.isField = true;
                entry.setOffset(currentHeapOffset); // Set the CORRECT absolute offset
                
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
                
                // APPEND to list
                TypeList newNode = new TypeList(methodType, null);
                if (membersHead == null) { membersHead = newNode; membersTail = newNode; }
                else { membersTail.tail = newNode; membersTail = newNode; }
            }
        }

        SymbolTable.getInstance().endScope();
        SymbolTable.getInstance().setInsideClass(false);
        tc.data_members = membersHead; // The list is now in declaration order [ID, age, salaries...]
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