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
		Temp src = exp.irMe();

		if (var instanceof AstExpVarSimple) {
			String name = ((AstExpVarSimple) var).name;
			SymbolTableEntry entry = SymbolTable.getInstance().findEntry(name);

			if (entry.scopeLevel == 0) {
				Ir.getInstance().AddIrCommand(new IrCommandStore(name, src, 0));
			} else {
				int finalOffset;
				if (entry.isParameter) {
					finalOffset = entry.offset;
				} else {
					finalOffset = -44 - (entry.offset * 4);
				}
				System.out.println("[DEBUG] Store Variable: " + name + " at offset: " + finalOffset);
				Ir.getInstance().AddIrCommand(new IrCommandStore(null, src, finalOffset));
			}
		}
		return null;
	}
}
