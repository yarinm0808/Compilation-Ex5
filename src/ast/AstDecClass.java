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

    public Type semantMe(){   
		SymbolTable.getInstance().beginScope();
		
		TypeList dataMembersType = null; // Use TypeList here
		if (dataMembers != null) {
			// Cast the Type returned by semantMe() to TypeList
			dataMembersType = (TypeList) dataMembers.semantMe();
		}

		// Now the types match for the TypeClass constructor
		TypeClass t = new TypeClass(null, name, dataMembersType);

		SymbolTable.getInstance().endScope();
		SymbolTable.getInstance().enter(name, t);

		return null;        
	}
}