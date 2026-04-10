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

    @Override
    public Temp irMe() {
        // 1. Resolve arguments first (Left-to-Right evaluation)
        List<Temp> argTemps = new ArrayList<>();
        for (AstExpList it = params; it != null; it = it.tail) {
            argTemps.add(it.head.irMe());
        }

        // 2. Standard Call Logic for Built-ins
        // Note: These don't return a value in the L language
        if (funcName.equals("PrintInt")) {
            Ir.getInstance().AddIrCommand(new IrCommandPrintInt(argTemps.get(0))); 
            return null; 
        }
        if (funcName.equals("PrintString")) { 
            Ir.getInstance().AddIrCommand(new IrCommandPrintString(argTemps.get(0)));
            return null;
        }

        Temp result = TempFactory.getInstance().getFreshTemp();

        // 3. DISTINGUISH: Virtual Method Call vs. Global Function
        if (base != null) {
            // --- VIRTUAL DISPATCH (Dynamic) ---
            
            // A. Get the object address (the 'base')
            Temp baseAddr = base.irMe();
            
            // B. Safety First: Null Pointer Check
            Ir.getInstance().AddIrCommand(new IrCommand_Check_Null_Ptr(baseAddr));
            
            // C. Use the Type metadata to find the fixed VMT OFFSET
            // Assuming your AstNode has a method to get its semant-resolved type
            TypeClass tc = (TypeClass) base.semantMe();
            int methodOffset = tc.findMethodOffset(funcName); 
            System.out.println(">> [IR CALL] Method: " + funcName + " | VMT Offset: " + methodOffset);
            // D. Runtime lookup
            Temp vmtPtr = TempFactory.getInstance().getFreshTemp();
            Temp funcAddr = TempFactory.getInstance().getFreshTemp();

            // [RUNTIME] vmtPtr = lw 0(baseAddr) -> Load VMT Pointer
            // We use LoadFromRegister to avoid refactoring your old Load command
            Ir.getInstance().AddIrCommand(new IrCommandLoadFromRegister(vmtPtr, baseAddr, 0));
            
            // [RUNTIME] funcAddr = lw methodOffset(vmtPtr) -> Load Function Address
            Ir.getInstance().AddIrCommand(new IrCommandLoadFromRegister(funcAddr, vmtPtr, methodOffset));

            // E. Prepare Arguments: 'this' pointer (baseAddr) MUST be the 1st parameter
            List<Temp> virtualArgs = new ArrayList<>();
            virtualArgs.add(baseAddr);   // The 'hidden' this pointer
            virtualArgs.addAll(argTemps); // The actual parameters

            // F. PERFORM THE CALL VIA REGISTER (jalr)
            Ir.getInstance().AddIrCommand(new IrCommandVirtualCall(result, funcAddr, virtualArgs));
            
            return result;

        } else {
            // --- STATIC DISPATCH (Global / Standalone) ---
            String callLabel = "func_" + funcName;
            
            // Standard call uses 'jal func_name'
            Ir.getInstance().AddIrCommand(new IrCommand_Call(result, callLabel, argTemps));
            
            return result;
        }
    }
}