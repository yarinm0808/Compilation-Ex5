package ast;
import ir.*;
import temp.Temp;
import types.*;
public class AstStmtReturn extends AstStmt
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public AstExp exp;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtReturn(AstExp exp)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.exp = exp;
	}

	/********************************************************/
	/* The printing message for a return statement AST node */
	/********************************************************/
	public void printMe()
	{
		/***********************************/
		/* AST NODE TYPE = AST RETURN STMT */
		/***********************************/
		System.out.print("AST NODE STMT RETURN\n");

		/*****************************/
		/* RECURSIVELY PRINT exp ... */
		/*****************************/
		if (exp != null) exp.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			"RETURN");

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (exp != null) AstGraphviz.getInstance().logEdge(serialNumber,exp.serialNumber);
	}

	@Override
	public Type semantMe(){
		if(this.exp != null) this.exp.semantMe();
		return null;
	}

	@Override
	public Temp irMe() {
		if (exp != null) {
			Temp val = exp.irMe();
			Ir.getInstance().AddIrCommand(new IrCommandReturnVal(val));
		}
		
		String endLabel = ControlFlowContext.getInstance().getCurrentFunctionEndLabel();
		Ir.getInstance().AddIrCommand(new IrCommandJump(endLabel));

		return null;
	}

}
