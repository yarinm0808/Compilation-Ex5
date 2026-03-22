package ast;

import types.*;
import symboltable.*;

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
        // 1. Resolve the father class if it exists (for inheritance)
        TypeClass fatherType = null;
        if (this.father != null) { // Changed 'superName' to 'this.father'
            Type t = SymbolTable.getInstance().find(this.father);
            if (t instanceof TypeClass) {
                fatherType = (TypeClass) t;
            } else {
                // Optional: throw error if father isn't a class
                throw new RuntimeException("ERROR(" + lineNumber + "): superclass " + this.father + " not found.");
            }
        }

        // 2. Initialize TypeClass: (fatherType, name, data_members)
        TypeClass tc = new TypeClass(fatherType, name, null);

        // 3. Populate the data_members list
        TypeList membersList = null;
        
        // Changed 'this.fields' to 'this.dataMembers' to match your class field
        for (AstDecList it = this.dataMembers; it != null; it = it.tail) {
            if (it.head instanceof AstDecVar) {
                AstDecVar varDec = (AstDecVar) it.head;
                Type fieldType = varDec.type.semantMe();
                
                // This is the bridge that fixes the "NOT FOUND" error!
                TypeClassVarDec bridge = new TypeClassVarDec(fieldType, varDec.name);
                membersList = new TypeList(bridge, membersList);
            }
            // If you handle methods, you'd add 'it.head instanceof AstDecFunc' here
        }

        // 4. Attach the list and register
        tc.data_members = membersList;
        SymbolTable.getInstance().enter(name, tc);

        return tc;
    }
}