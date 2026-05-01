package ast;
import types.*;
import temp.*;
import ir.*;

import symboltable.SymbolTable;
import symboltable.SymbolTableEntry;

public class AstStmtAssign extends AstStmt {
    public AstExpVar var;
    public AstExp exp;
    public int cachedFieldOffset = -1;

    public AstStmtAssign(AstExpVar var, AstExp exp) {
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.print("====================== stmt -> var ASSIGN exp SEMICOLON\n");
        this.var = var;
        this.exp = exp;
    }

    public void printMe() {
        System.out.print("AST NODE ASSIGN STMT\n");
        if (var != null) var.printMe();
        if (exp != null) exp.printMe();
        AstGraphviz.getInstance().logNode(serialNumber, "ASSIGN\nleft := right\n");
        AstGraphviz.getInstance().logEdge(serialNumber, var.serialNumber);
        AstGraphviz.getInstance().logEdge(serialNumber, exp.serialNumber);
    }

    @Override
    public Type semantMe() {
        Type tVar = null;
        Type tExp = null;

        if (var != null) {
            tVar = var.semantMe();
            
            // --- Caching Logic ---
            if (var instanceof AstExpVarField) {
                AstExpVarField fieldVar = (AstExpVarField) var;
                Type baseType = fieldVar.var.semantMe();
                if (baseType instanceof TypeClass) {
                    this.cachedFieldOffset = ((TypeClass) baseType).findFieldOffset(fieldVar.fieldName);
                }
            }
        }
        
        if (exp != null) {
            tExp = exp.semantMe();
        }

        if (tVar == null || tExp == null) {
            return null; 
        }

        // --- The Universal Checker ---
        boolean isValid = false;

        if (tVar == tExp) {
            isValid = true;
        } else if (tVar != null && tExp != null && tVar.name != null && tVar.name.equals(tExp.name)) {
            isValid = true;
        } else if (tExp instanceof TypeNil && (tVar instanceof TypeClass || tVar instanceof TypeArray)) {
            isValid = true;
        } else if (tVar != null && tExp != null && (tVar.isCompatible(tExp) || tExp.isCompatible(tVar))) {
            isValid = true;
        }

        if (!isValid) {
            throw new RuntimeException("ERROR(" + lineNumber + ")");
        }

        return null;
    }

    @Override
    public Temp irMe() {
        Temp lhsBasePtr = null;
        Temp lhsIndexVal = null;
        int fieldOffset = -1;
        
        if (var instanceof AstExpVarSubscript) {
            AstExpVarSubscript sub = (AstExpVarSubscript) var;
            lhsBasePtr = sub.var.irMe();
            lhsIndexVal = sub.subscript.irMe(); 
            Ir.getInstance().AddIrCommand(new IrCommand_Check_Null_Ptr(lhsBasePtr));
            Ir.getInstance().AddIrCommand(new IrCommandBoundsCheck(lhsBasePtr, lhsIndexVal));
        }
        else if (var instanceof AstExpVarField) {
            AstExpVarField fieldVar = (AstExpVarField) var;
            lhsBasePtr = fieldVar.var.irMe();
            Ir.getInstance().AddIrCommand(new IrCommand_Check_Null_Ptr(lhsBasePtr));
            
            // USE THE CACHED VALUE INSTEAD OF CALLING semantMe()
            fieldOffset = this.cachedFieldOffset; 
        }

        Temp srcValue = exp.irMe();

        if (var instanceof AstExpVarSimple) {
            // Safely delegate to the method we added in AstExpVarSimple
            ((AstExpVarSimple) var).irStore(srcValue);
        } 
        else if (var instanceof AstExpVarField) {
            Ir.getInstance().AddIrCommand(new IrCommandStoreField(lhsBasePtr, fieldOffset, srcValue));
        } 
        else if (var instanceof AstExpVarSubscript) {
            Ir.getInstance().AddIrCommand(new IrCommandStoreSubscript(lhsBasePtr, lhsIndexVal, srcValue));
        }

        return null; 
    }
}