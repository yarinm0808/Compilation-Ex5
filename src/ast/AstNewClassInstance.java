package ast;
import types.*;
import ir.*;
import symboltable.SymbolTable;
import temp.Temp;
import temp.TempFactory;

public class AstNewClassInstance extends AstExp {
    public AstType type;
    private TypeClass cachedType;

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
        this.cachedType = (TypeClass) t;
        return t;
    }

    @Override
    public Temp irMe() {
        // 1. Calculate size: 4 bytes (VMT Pointer) + (Fields * 4)
        // Your TypeClass.getFieldCount() already handles the hierarchy
        int sizeInBytes = 4 + (cachedType.getFieldCount() * 4);
        
        Temp addressTemp = TempFactory.getInstance().getFreshTemp();
        
        // 2. Allocate memory on the heap
        Ir.getInstance().AddIrCommand(new IrCommand_Malloc(addressTemp, sizeInBytes));

        // 3. THE VMT STAMP (CRITICAL)
        // We store the address of the class VMT label at offset 0
        // e.g., "Son_VMT"
        String vmtLabel = cachedType.name + "_VMT";
        Ir.getInstance().AddIrCommand(new IrCommandStoreVmt(addressTemp, vmtLabel));

        // 4. Initialize fields (starting from offset 4)
        generateFieldInitializers(addressTemp, cachedType);

        return addressTemp;
    }

    /**
     * Recursive helper to initialize fields from the root father down to the child
     */
    private void generateFieldInitializers(Temp objectAddr, TypeClass currentClass) {
        // [A] Base Case: If there's a father, initialize his fields first
        if (currentClass.father != null) {
            generateFieldInitializers(objectAddr, currentClass.father);
        }

        // [B] Initialize this class's local fields
        // Loop through the data members of the current class level
        for (TypeList it = currentClass.data_members; it != null; it = it.tail) {
            if (it.head instanceof TypeClassVarDec) {
                TypeClassVarDec vd = (TypeClassVarDec) it.head;

                // Does this field have an initial value expression (e.g., ':= 10')?
                // NOTE: You must ensure TypeClassVarDec stores the initialValue AST node!
                if (vd.initExp != null) {
                    // 1. Evaluate the expression (e.g., generate the code for '10')
                    Temp valTemp = vd.initExp.irMe();

                    // 2. Get the field's offset in the object
                    int offset = currentClass.findFieldOffset(vd.name);

                    // 3. Store the value into the heap at that offset
                    // Generates: sw $valTemp, offset($addressTemp)
                    Ir.getInstance().AddIrCommand(new IrCommandStoreField(objectAddr, offset, valTemp));
                }
            }
        }
    }

}