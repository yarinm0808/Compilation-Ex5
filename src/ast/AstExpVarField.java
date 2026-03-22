package ast;

import temp.Temp;
import temp.TempFactory;
import types.*;
import ir.*;

public class AstExpVarField extends AstExpVar
{
	public AstExpVar var;
	public String fieldName;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpVarField(AstExpVar var, String fieldName)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		System.out.format("====================== var -> var DOT ID( %s )\n",fieldName);
		this.var = var;
		this.fieldName = fieldName;
	}

	/*************************************************/
	/* The printing message for a field var AST node */
	/*************************************************/
	public void printMe()
	{
		/*********************************/
		/* AST NODE TYPE = AST FIELD VAR */
		/*********************************/
		System.out.format("FIELD\nNAME\n(___.%s)\n",fieldName);

		/**********************************************/
		/* RECURSIVELY PRINT VAR, then FIELD NAME ... */
		/**********************************************/
		if (var != null) var.printMe();

		/**********************************/
		/* PRINT to AST GRAPHVIZ DOT file */
		/**********************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("FIELD\nVAR\n___.%s",fieldName));

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (var  != null) AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
	}

	public Type semantMe() {
		Type baseType = var.semantMe(); // var is 'p'
		
		// DEBUG: What did we get for 'p'?
		System.out.println(">> [DEBUG] Field Access on '" + fieldName + "'. Base variable type is: " + 
			(baseType != null ? baseType.getClass().getSimpleName() : "NULL"));

		if (!(baseType instanceof TypeClass)) {
			throw new RuntimeException("ERROR(" + lineNumber + ")");
		}

		TypeClass tc = (TypeClass) baseType;
		Type fieldType = tc.findFieldType(fieldName);
		
		// DEBUG: Did we find the field?
		System.out.println(">> [DEBUG] findFieldType('" + fieldName + "') result: " + 
			(fieldType != null ? "FOUND" : "NOT FOUND"));

		if (fieldType == null) {
			throw new RuntimeException("ERROR(" + lineNumber + ")");
		}
		
		return fieldType;
	}

	public Temp irMe() {
		// 1. Load the address of 'p' (this is our baseAddr)
		Temp baseAddr = var.irMe();

		// 2. Add your Null Pointer Check (Mandatory for Ex 5!)
		Ir.getInstance().AddIrCommand(new IrCommand_Check_Null_Ptr(baseAddr));

		// 3. Get the memory offset of 'age'
		TypeClass tc = (TypeClass) var.semantMe();
		int offset = tc.findFieldOffset(fieldName);

		// 4. Create destination Temp
		Temp result = TempFactory.getInstance().getFreshTemp();

		// 5. Use the HEAP load command
		Ir.getInstance().AddIrCommand(new IrCommandLoadField(result, baseAddr, offset));

		return result;
	}
}
