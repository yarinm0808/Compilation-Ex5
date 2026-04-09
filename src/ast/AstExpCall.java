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
        TypeFunction funcType = null;

        // 1. Resolve the Function/Method Signature
        if (base != null) {
            // Method Call: moish.getAverage()
            Type baseType = base.semantMe();
            System.out.println(">> [DEBUG] Method Call: " + funcName + " on object of type: " + baseType);
            if (!(baseType instanceof TypeClass)) {
                throw new RuntimeException("ERROR(" + lineNumber + "): Cannot call method on non-class type");
            }
            
            // Find the method in the class hierarchy (including father classes)
            Type member = ((TypeClass) baseType).findFieldType(funcName);
            if (!(member instanceof TypeFunction)) {
                throw new RuntimeException("ERROR(" + lineNumber + "): Method " + funcName + " not found");
            }
            funcType = (TypeFunction) member;
        } else {
            // Global Call: PrintInt(10)
            SymbolTableEntry entry = SymbolTable.getInstance().findEntry(funcName);
            if (entry == null || !(entry.type instanceof TypeFunction)) {
                throw new RuntimeException("ERROR(" + lineNumber + "): Function " + funcName + " not defined");
            }
            funcType = (TypeFunction) entry.type;
        }

        // 2. Validate Parameters
        AstExpList itActual = params;  // The values passed: (96, 100, ...)
        TypeList itFormal = funcType.params; // The required types: (int, int, ...)

        while (itActual != null && itFormal != null) {
            Type tActual = itActual.head.semantMe();
            Type tFormal = itFormal.head;

            // --- THE NEW CHECK ---
            // Ask the formal type: "Is this actual value compatible with you?"
            if (!tFormal.isCompatible(tActual)) {
                throw new RuntimeException("ERROR(" + lineNumber + "): Parameter type mismatch for " + funcName);
            }

            itActual = itActual.tail;
            itFormal = itFormal.tail;
        }

        // 3. Check for Argument Count Mismatch
        if (itActual != null || itFormal != null) {
            throw new RuntimeException("ERROR(" + lineNumber + "): Wrong number of arguments for " + funcName);
        }

        return funcType.returnType;
    }

    public Temp irMe() {
        // 1. Evaluate the base (the object) if it's a method call
        Temp baseAddr = null;
        String callLabel = "func_" + funcName; // Default for global functions

        if (base != null) {
            baseAddr = base.irMe();
            Ir.getInstance().AddIrCommand(new IrCommand_Check_Null_Ptr(baseAddr));
            
            // Use the Type metadata to find the CORRECT class label
            TypeClass tc = (TypeClass) base.semantMe();
            Type member = tc.findFieldType(funcName);
            
            if (member instanceof TypeFunction) {
                TypeFunction tf = (TypeFunction) member;
                // This pulls 'Person' if inherited, or 'Student' if overridden
                callLabel = tf.definingClassName + "_" + funcName;
            }
        }

        // 2. Standard Call Logic for Built-ins
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

        // 3. Collect Arguments
        List<Temp> argTemps = new ArrayList<>();
        
        // --- THE HIDDEN PARAMETER ---
        // If it's a method call, 'this' (baseAddr) must be the FIRST argument
        if (baseAddr != null) {
            argTemps.add(baseAddr);
        }

        for (AstExpList it = params; it != null; it = it.tail) {
            argTemps.add(it.head.irMe());
        }

        // 4. Generate the Call
        Temp result = TempFactory.getInstance().getFreshTemp();
        
        // Use the potentially mangled callLabel
        Ir.getInstance().AddIrCommand(new IrCommand_Call(result, callLabel, argTemps));
        
        return result;
    }
}