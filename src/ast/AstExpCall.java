package ast;

import temp.*;
import types.*; 
import java.util.ArrayList;
import java.util.List;
import ir.*;
import symboltable.SymbolTable;
import symboltable.SymbolTableEntry;

public class AstExpCall extends AstExp
{
    public AstExpVar base; // Added for Part 4 (v.foo)
    public String funcName;
    public AstExpList params;

    // Constructor for Global Calls: foo(1, 2)
    public AstExpCall(String funcName, AstExpList params)
    {
        serialNumber = AstNodeSerialNumber.getFresh();
        this.base = null; 
        this.funcName = funcName;
        this.params = params;
    }

    // Constructor for Method Calls: v.foo(1, 2)
    public AstExpCall(AstExpVar base, String funcName, AstExpList params)
    {
        serialNumber = AstNodeSerialNumber.getFresh();
        this.base = base;
        this.funcName = funcName;
        this.params = params;
    }

    public void printMe()
    {
        System.out.format("CALL(%s)\n", funcName);
        if (base != null) {
            System.out.print("BASE:\n");
            base.printMe();
        }
        if (params != null) params.printMe();
        
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("CALL(%s)\nWITH", funcName));
        
        if (base != null) AstGraphviz.getInstance().logEdge(serialNumber, base.serialNumber);
        if (params != null) AstGraphviz.getInstance().logEdge(serialNumber, params.serialNumber);
    }

    @Override
    public Type semantMe() {
        // 1. If it's a method call (base != null), you must check the class scope
        if (base != null) {
            Type baseType = base.semantMe();
            if (!(baseType instanceof TypeClass)) {
                throw new RuntimeException("ERROR(" + lineNumber + "): method call on non-class type.");
            }
            // Logic to find funcName inside baseType goes here...
            // For now, let's assume global resolution or simple class methods
        }

        SymbolTableEntry funcEntry = SymbolTable.getInstance().findEntry(funcName);
        if (funcEntry == null || !(funcEntry.type instanceof TypeFunction)) {
            throw new RuntimeException("ERROR(" + lineNumber + ")");
        }
        
        TypeFunction funcType = (TypeFunction) funcEntry.type;
        for (AstExpList it = params; it != null; it = it.tail) {
            it.head.semantMe(); 
        }

        return funcType.returnType;
    }

    public Temp irMe() {
        // [Part 4 Fix] Mandatory Nil Check for Method Calls
        if (base != null) {
            Temp baseAddr = base.irMe();
            // This is the core of Exercise 5:
            Ir.getInstance().AddIrCommand(new IrCommand_Check_Null_Ptr(baseAddr));
        }

        // Standard Call Logic
        if (funcName.equals("PrintInt")) {
            Temp argTemp = params.head.irMe();
            Ir.getInstance().AddIrCommand(new IrCommandPrintInt(argTemp)); 
            return null; 
        }
        
        if (funcName.equals("PrintString")) { 
            Temp addr = params.head.irMe();
            Ir.getInstance().AddIrCommand(new IrCommandPrintString(addr));
            return null;
        }

        List<Temp> argTemps = new ArrayList<>();
        for (AstExpList it = params; it != null; it = it.tail) {
            argTemps.add(it.head.irMe());
        }

        Temp result = TempFactory.getInstance().getFreshTemp();
        Ir.getInstance().AddIrCommand(new IrCommand_Call(result, "func_" + funcName, argTemps));
        
        return result;
    }
}