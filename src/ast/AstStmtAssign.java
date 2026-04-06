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

	public Type semantMe(){
		Type t1 = null;
		Type t2 = null;
		
		if (var != null) t1 = var.semantMe();
		if (exp != null) t2 = exp.semantMe();
		
		if (t1 != t2)
		{
			System.out.format(">> ERROR [%d:%d] type mismatch for var := exp\n",6,6);				
		}
		return null;
	}

	public Temp irMe() {
		// 1. Generate the value we want to store (e.g., the number 5)
		Temp src = exp.irMe();

		if (var instanceof AstExpVarSimple) {
			// Use the entry ALREADY in the node!
			SymbolTableEntry entry = ((AstExpVarSimple) var).entry; 

			if (entry.scopeLevel > 0) {
				// Local variable or Parameter - USE THE OFFSET
				int finalOffset = (entry.isParameter) ? entry.offset : -44 - (entry.offset * 4);
				Ir.getInstance().AddIrCommand(new IrCommandStore(null, src, finalOffset));
			} else {
				// Global variable - USE THE NAME
				Ir.getInstance().AddIrCommand(new IrCommandStore(entry.name, src, 0));
			}
		}
		else if (var instanceof AstExpVarField) {
			// --- NEW LOGIC FOR CLASS FIELDS (p.age := 5) ---
			AstExpVarField fieldVar = (AstExpVarField) var;

			// 1. Get the address of the object (e.g., load the pointer 'p' from the stack)
			Temp baseAddr = fieldVar.var.irMe();

			// 2. Add Null Pointer Check (Mandatory!)
			Ir.getInstance().AddIrCommand(new IrCommand_Check_Null_Ptr(baseAddr));

			// 3. Find the offset of the field 'age' within the class
			TypeClass tc = (TypeClass) fieldVar.var.semantMe();
			int offset = tc.findFieldOffset(fieldVar.fieldName);

			// 4. Generate the STORE command to the heap
			// This generates: sw $src, offset($baseAddr)
			Ir.getInstance().AddIrCommand(new IrCommandStoreField(baseAddr, offset, src));
		}
		else if (var instanceof AstExpVarSubscript) {
			AstExpVarSubscript lhsSub = (AstExpVarSubscript) var;
			
			// 3. Evaluate the base array pointer and the index
			Temp basePtr = lhsSub.var.irMe();
			Temp indexVal = lhsSub.subscript.irMe();

			// 4. ADD THE NEW COMMAND!
			Ir.getInstance().AddIrCommand(new IrCommandStoreSubscript(basePtr, indexVal, src));
		}
		
		return null;
	}
}
