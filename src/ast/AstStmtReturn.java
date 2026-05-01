package ast;
import ir.*;
import symboltable.SymbolTable;
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
    public Type semantMe() {
        Type expectedType = SymbolTable.getInstance().currentExpectedReturnType;
        Type actualType = (exp != null) ? exp.semantMe() : TypeVoid.getInstance();

        boolean isValid = false;

        // 1. Exact match in memory
        if (expectedType == actualType) {
            isValid = true;
        } 
        // 2. Exact match by name
        else if (expectedType != null && actualType != null && expectedType.name != null && expectedType.name.equals(actualType.name)) {
            isValid = true;
        } 
        // 3. Nil returned for Class or Array
        else if (actualType instanceof TypeNil && (expectedType instanceof TypeClass || expectedType instanceof TypeArray)) {
            isValid = true;
        } 
        // 4. Inheritance / Subtyping
        else if (expectedType != null && actualType != null && (actualType.isCompatible(expectedType) || expectedType.isCompatible(actualType))) {
            isValid = true;
        }

        // If it doesn't match the expected return type, throw the exact error!
        if (!isValid) {
            throw new RuntimeException("ERROR(" + lineNumber + ")");
        }

        // Tag that we found a valid return statement!
        SymbolTable.getInstance().foundReturn = true; 
        
        return null;
    }

	@Override
	public Temp irMe() {
		String endLabel = ControlFlowContext.getInstance().getCurrentFunctionEndLabel();

		if (exp != null) {
			Temp val = exp.irMe();
			// Match your constructor: (Temp, String)
			Ir.getInstance().AddIrCommand(new IrCommandReturnVal(val, endLabel));
		} else {
			// For void returns, just jump to the exit
			Ir.getInstance().AddIrCommand(new IrCommandJump(endLabel));
		}
		return null;
	}

}
