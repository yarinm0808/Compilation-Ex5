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
        // 1. Resolve the father class if it exists
        TypeClass fatherType = null;
        if (this.father != null) {
            Type t = SymbolTable.getInstance().find(this.father);
            if (t instanceof TypeClass) {
                fatherType = (TypeClass) t;
            } else {
                throw new RuntimeException("ERROR(" + lineNumber + "): superclass " + this.father + " not found.");
            }
        }

        // 2. Initialize TypeClass
        TypeClass tc = new TypeClass(fatherType, name, null);

        // --- THE FIX: REGISTER THE CLASS NOW ---
        // This allows the fields inside to "see" the class name during their own semantic pass.
        SymbolTable.getInstance().enter(name, tc);

        // 3. Populate the data_members list
        TypeList membersList = null;
        for (AstDecList it = this.dataMembers; it != null; it = it.tail) {
            if (it.head instanceof AstDecVar) {
                AstDecVar varDec = (AstDecVar) it.head;
                
                // Now, when this calls semantMe(), it will find 'IntList' in the table!
                Type fieldType = varDec.type.semantMe();
                
                TypeClassVarDec fieldEntry = new TypeClassVarDec(fieldType, varDec.name);
                membersList = new TypeList(fieldEntry, membersList);
            }
        }

        // 4. Attach the completed list to the type object
        tc.data_members = membersList;

        return tc;
    }
}