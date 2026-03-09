package ast;

import temp.*;
import types.*;

public class AstStmtCall extends AstStmt
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public AstExpCall callExp;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstStmtCall(AstExpCall callExp)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.callExp = callExp;
	}
	
	public void printMe()
	{
		callExp.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("STMT\nCALL"));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		AstGraphviz.getInstance().logEdge(serialNumber,callExp.serialNumber);
	}

	@Override
	public Type semantMe() {
		// This MUST call semantMe on the underlying expression 
		// to ensure all variables inside it are linked to the Symbol Table.
		if (callExp != null) {
			callExp.semantMe();
		}
		return null;
	}

	public Temp irMe()
	{
		if (callExp != null) callExp.irMe();

		return null;
	}
}