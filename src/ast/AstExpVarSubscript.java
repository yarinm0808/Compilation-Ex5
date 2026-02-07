package ast;

import ir.Ir;
import ir.IrCommandLoadSubscript;
import temp.Temp;
import temp.TempFactory;
import types.*;
public class AstExpVarSubscript extends AstExpVar
{
	public AstExpVar var;
	public AstExp subscript;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpVarSubscript(AstExpVar var, AstExp subscript)
	{
		System.out.print("====================== var -> var [ exp ]\n");
		this.var = var;
		this.subscript = subscript;
	}

	/*****************************************************/
	/* The printing message for a subscript var AST node */
	/*****************************************************/
	public void printMe()
	{
		/*************************************/
		/* AST NODE TYPE = AST SUBSCRIPT VAR */
		/*************************************/
		System.out.print("AST NODE SUBSCRIPT VAR\n");

		/****************************************/
		/* RECURSIVELY PRINT VAR + SUBSRIPT ... */
		/****************************************/
		if (var != null) var.printMe();
		if (subscript != null) subscript.printMe();
	}
	@Override
	public Type semantMe() {
		// 1. Resolve the variable. It MUST be a TypeArray.
		Type t = var.semantMe();
		if (!(t instanceof TypeArray)) {
			throw new RuntimeException("ERROR(" + lineNumber + ")");
		}

		// 2. Resolve the subscript. It MUST be an integer.
		Type s = subscript.semantMe();
		if (!(s instanceof TypeInt)) {
			throw new RuntimeException("ERROR(" + lineNumber + ")");
		}

		// 3. The result of indexing an array is the type of its elements.
		return ((TypeArray) t).elementType;
	}
	@Override
	public Temp irMe() {
		// 1. Get the base address of the array
		Temp base = var.irMe();
		
		// 2. Get the index value
		Temp index = subscript.irMe();
		
		// 3. Create a result temp
		Temp res = TempFactory.getInstance().getFreshTemp();

		// 4. Generate IR to load the value.
		// Note: You may need a new IrCommandLoadSubscript that handles:
		// lw res, (index+1)*4(base)
		Ir.getInstance().AddIrCommand(new IrCommandLoadSubscript(res, base, index));
		
		return res;
	}
}
