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

	public Temp irMe(){
        // 1. Evaluate the expression to get the value we want to store
        Temp src = exp.irMe();

        // 2. We need to handle 'var'. If it's a simple variable:
        if (var instanceof AstExpVarSimple)
        {
            String name = ((AstExpVarSimple) var).name;
            
            // Look up the metadata for this variable
            SymbolTableEntry entry = SymbolTable.getInstance().findEntry(name);

            if (entry != null && entry.offset != 0)
            {
                // LOCAL/PARAM: varName is null, we use the offset
                Ir.getInstance().AddIrCommand(new IrCommandStore(null, src, entry.offset));
            }
            else
            {
                // GLOBAL: use the varName, offset is 0
                Ir.getInstance().AddIrCommand(new IrCommandStore(name, src, 0));
            }
        }
        else
        {
            // Handle array access or field access here later if needed
            // For now, simple variables are fixed!
        }

        return null;
    }
}
