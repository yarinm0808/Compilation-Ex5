package ast;
import types.*;
import temp.*;
import ir.*;

import symboltable.SymbolTable;
import symboltable.SymbolTableEntry;

public class AstStmtAssign extends AstStmt
{
	/***************/
	/*  var := exp */
	/***************/
	public AstExpVar var;
	public AstExp exp;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtAssign(AstExpVar var, AstExp exp)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== stmt -> var ASSIGN exp SEMICOLON\n");

		/*******************************/
		/* COPY INPUT DATA MENBERS ... */
		/*******************************/
		this.var = var;
		this.exp = exp;
	}

	/*********************************************************/
	/* The printing message for an assign statement AST node */
	/*********************************************************/
	public void printMe()
	{
		/********************************************/
		/* AST NODE TYPE = AST ASSIGNMENT STATEMENT */
		/********************************************/
		System.out.print("AST NODE ASSIGN STMT\n");

		/***********************************/
		/* RECURSIVELY PRINT VAR + EXP ... */
		/***********************************/
		if (var != null) var.printMe();
		if (exp != null) exp.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			"ASSIGN\nleft := right\n");
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
		AstGraphviz.getInstance().logEdge(serialNumber,exp.serialNumber);
	}


	@Override
	public Type semantMe() {
		Type tVar = null;
		Type tExp = null;

		if (var != null) tVar = var.semantMe();
		if (exp != null) tExp = exp.semantMe();

		// Safety check for failed sub-semantics
		if (tVar == null || tExp == null) {
			return null; 
		}

		// --- THE NEW CHECK ---
		// Ask the variable's type: "Can I hold this expression's value?"
		if (!tVar.isCompatible(tExp)) {
			throw new RuntimeException("ERROR(" + lineNumber + "): Type mismatch in assignment");
		}

		return null; // Statements don't have a type
	}

	@Override
	public Temp irMe() {
		// -----------------------------------------------------------------
		// [STEP 1] EVALUATE LHS TARGET FIRST
		// We do this to "lock in" the memory address before the RHS changes it.
		// -----------------------------------------------------------------
		Temp lhsBasePtr = null;
		Temp lhsIndexVal = null;
		int fieldOffset = -1;

		if (var instanceof AstExpVarSubscript) {
			AstExpVarSubscript sub = (AstExpVarSubscript) var;
			// Capture the array pointer
			lhsBasePtr = sub.var.irMe();
			// CAPTURE THE INDEX NOW (e.g., moish.age = 10)
			lhsIndexVal = sub.subscript.irMe(); 
			
			Ir.getInstance().AddIrCommand(new IrCommand_Check_Null_Ptr(lhsBasePtr));
		} 
		else if (var instanceof AstExpVarField) {
			AstExpVarField fieldVar = (AstExpVarField) var;
			// Capture the object pointer
			lhsBasePtr = fieldVar.var.irMe();
			Ir.getInstance().AddIrCommand(new IrCommand_Check_Null_Ptr(lhsBasePtr));
			
			// Find the offset of the field within the class
			TypeClass tc = (TypeClass) fieldVar.var.semantMe(); 
			fieldOffset = tc.findFieldOffset(fieldVar.fieldName);
		}

		// -----------------------------------------------------------------
		// [STEP 2] EVALUATE RHS VALUE SECOND
		// This is where moish.birthday() happens, changing age from 10 to 11.
		// -----------------------------------------------------------------
		Temp srcValue = exp.irMe();

		// -----------------------------------------------------------------
		// [STEP 3] PERFORM THE STORE
		// Now we use the captured components from Step 1 and the value from Step 2.
		// -----------------------------------------------------------------
		if (var instanceof AstExpVarSimple) {
			SymbolTableEntry entry = ((AstExpVarSimple) var).entry;

			if (entry.isField) {
				// CASE A: Implicit 'this' (e.g., age := 5 inside a Person method)
				Temp tThis = TempFactory.getInstance().getFreshTemp();
				// In this project structure, 'this' is at offset 8($fp)
				Ir.getInstance().AddIrCommand(new IrCommandLoad(tThis, null, 8));
				Ir.getInstance().AddIrCommand(new IrCommandStoreField(tThis, entry.offset, srcValue));
			} 
			else if (entry.scopeLevel == 0) {
				// CASE B: Global variable
				Ir.getInstance().AddIrCommand(new IrCommandStore(entry.name, srcValue, 0));
			} 
			else {
				// CASE C: Local variable or Parameter
				int finalOffset;
				if (entry.isParameter) {
					// Parameters start at 8($fp) and go up
					finalOffset = 8 + (entry.offset * 4);
				} else {
					// Local variables start at -44($fp) and go down
					finalOffset = -44 - (entry.offset * 4);
				}
				Ir.getInstance().AddIrCommand(new IrCommandStore(null, srcValue, finalOffset));
			}
		} 
		else if (var instanceof AstExpVarField) {
			// Explicit field access: p.age := srcValue
			Ir.getInstance().AddIrCommand(new IrCommandStoreField(lhsBasePtr, fieldOffset, srcValue));
		} 
		else if (var instanceof AstExpVarSubscript) {
			// Array access: arr[i] := srcValue
			Ir.getInstance().AddIrCommand(new IrCommandStoreSubscript(lhsBasePtr, lhsIndexVal, srcValue));
		}

		return null; 
	}
}
