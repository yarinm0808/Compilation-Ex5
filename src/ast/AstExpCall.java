package ast;

import temp.*;
import types.*; // [FIX] Import everything from the types package
import java.util.ArrayList;
import java.util.List;
import ir.*;
import symboltable.SymbolTable;
import symboltable.SymbolTableEntry;

public class AstExpCall extends AstExp
{
    public String funcName;
    public AstExpList params;

    public AstExpCall(String funcName, AstExpList params)
    {
        serialNumber = AstNodeSerialNumber.getFresh();
        this.funcName = funcName;
        this.params = params;
    }

    public void printMe()
    {
        System.out.format("CALL(%s)\nWITH:\n",funcName);
        if (params != null) params.printMe();
        
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("CALL(%s)\nWITH",funcName));
        
        if (params != null) AstGraphviz.getInstance().logEdge(serialNumber,params.serialNumber);
    }

    @Override
    public Type semantMe() {
        // [FIX] Changed 'name' to 'funcName' to match your data member
        SymbolTableEntry funcEntry = SymbolTable.getInstance().findEntry(funcName);
        
        if (funcEntry == null || !(funcEntry.type instanceof TypeFunction)) {
            throw new RuntimeException("ERROR(" + lineNumber + "): function " + funcName + " not defined.");
        }
        TypeFunction funcType = (TypeFunction) funcEntry.type;

        // [FIX] Changed 'args' to 'params' to match your data member
        for (AstExpList it = params; it != null; it = it.tail) {
            it.head.semantMe(); 
        }

        return funcType.returnType;
    }

    public Temp irMe() {
        // Case 1: Special Library Function (PrintInt/PrintString)
        if (funcName.equals("PrintInt")) {
            Temp argTemp = params.head.irMe();
            Ir.getInstance().AddIrCommand(new IrCommandPrintInt(argTemp)); 
            return null; 
        }
        else{
            if (funcName.equals("PrintString")) { 
                AstExp arg = params.head; // The expression 'z'
                Temp addr = arg.irMe();
                Ir.getInstance().AddIrCommand(new IrCommandPrintString(addr));
                return null;
            }
        }

        // Case 2: User Defined Function (f, etc.)
        List<Temp> argTemps = new ArrayList<>();
        for (AstExpList it = params; it != null; it = it.tail) {
            argTemps.add(it.head.irMe());
        }

        Temp result = TempFactory.getInstance().getFreshTemp();
        // This command handles pushing argTemps to the stack
        Ir.getInstance().AddIrCommand(new IrCommand_Call(result, "func_" + funcName, argTemps));
        
        return result;
    }
}