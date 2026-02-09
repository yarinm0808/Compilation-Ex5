package ast;

import temp.*;
import ir.*;

public class AstExpCall extends AstExp
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public String funcName;
	public AstExpList params;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpCall(String funcName, AstExpList params)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.funcName = funcName;
		this.params = params;
	}

	/************************************************/
	/* The printing message for a call exp AST node */
	/************************************************/
	public void printMe()
	{
		/********************************/
		/* AST NODE TYPE = AST CALL EXP */
		/********************************/
		System.out.format("CALL(%s)\nWITH:\n",funcName);

		/***************************************/
		/* RECURSIVELY PRINT params + body ... */
		/***************************************/
		if (params != null) params.printMe();
		
		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("CALL(%s)\nWITH",funcName));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		AstGraphviz.getInstance().logEdge(serialNumber,params.serialNumber);
	}

	public Temp irMe() {
		// 1. Evaluate and Push each parameter onto the stack
		// We iterate through the params list
		for (AstExpList it = params; it != null; it = it.tail) {
			Temp paramTemp = it.head.irMe();
			// Emit a command to push this temp onto the stack
			Ir.getInstance().AddIrCommand(new IrCommandPush(paramTemp));
		}

		// 2. Execute the Call
		// This will translate to 'jal func_name' in MIPS
		Ir.getInstance().AddIrCommand(new IrCommandCall(funcName));
		// 3. Clean up the stack
		// If we pushed 3 params (12 bytes), we must move the stack pointer back
		int paramCount = 0;
		for (AstExpList it = params; it != null; it = it.tail) { paramCount++; }
		if (paramCount > 0) {
			Ir.getInstance().AddIrCommand(new IrCommandStackPointUpdate(paramCount * 4));
		}

		// 4. Capture the return value
		// Functions put their result in $v0. We move $v0 into a fresh Temp.
		Temp result = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandGetReturnValue(result));

		return result;
	}
}
