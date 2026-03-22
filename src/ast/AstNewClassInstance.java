package ast;
import types.*;
import ir.*;
import symboltable.SymbolTable;
import temp.Temp;
import temp.TempFactory;

public class AstNewClassInstance extends AstExp {
    public AstType type;

    public AstNewClassInstance(AstType type, int lineNumber) {
        this.serialNumber = AstNodeSerialNumber.getFresh();
        this.type = type;
        this.lineNumber = lineNumber;
    }

    public void printMe() {
        System.out.print("NEW CLASS INSTANCE\n");
        if (type != null) type.printMe();
        
        AstGraphviz.getInstance().logNode(serialNumber, "NEW\nCLASS");
        if (type != null) AstGraphviz.getInstance().logEdge(serialNumber, type.serialNumber);
    }

    public Type semantMe() {
        // 1. Look up the type in the Symbol Table (e.g., "Person")
        // Note: your AstType might have a name field or a specific ID
        String typeName = type.getTypeName(); 
        Type t = SymbolTable.getInstance().find(typeName);

        // 2. Ensure the type exists and is a Class
        if (t == null || !(t instanceof TypeClass)) {
            // Error(line): Cannot instantiate a non-class type
            throw new RuntimeException("ERROR(" + lineNumber + ")");
        }

        // 3. Return the class type so the parent (Assignment) can verify type compatibility
        return t;
    }
    public Temp irMe() {
        // 1. Retrieve the TypeClass from the Symbol Table
        TypeClass tc = (TypeClass) SymbolTable.getInstance().find(type.getTypeName());

        // 2. Calculate the number of fields (including inherited ones!)
        int fieldCount = tc.getFieldCount(); 
        int sizeInBytes = fieldCount * 4;

        // 3. Allocate a temporary to hold the address of the new object
        Temp addressTemp = TempFactory.getInstance().getFreshTemp();

        // 4. Generate IR Command for Malloc
        // This will eventually translate to MIPS Syscall 9 (sbrk)
        Ir.getInstance().AddIrCommand(new IrCommand_Malloc(addressTemp, sizeInBytes));

        return addressTemp;
    }

}