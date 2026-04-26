package ast;
import types.*;
import temp.*;
import ir.*;

import symboltable.SymbolTable;
import symboltable.SymbolTableEntry;

public class AstStmtAssign extends AstStmt {
    public AstExpVar var;
    public AstExp exp;

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

        // CRITICAL FIX: Guarantee that semantMe runs on the variable so 'entry' is populated!
        if (var != null) {
            tVar = var.semantMe();
        }
        if (exp != null) {
            tExp = exp.semantMe();
        }

        if (tVar == null || tExp == null) {
            return null; 
        }

        if (!tVar.isCompatible(tExp)) {
            throw new RuntimeException("ERROR(" + lineNumber + "): Type mismatch in assignment");
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
            TypeClass tc = (TypeClass) fieldVar.var.semantMe(); 
            fieldOffset = tc.findFieldOffset(fieldVar.fieldName);
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