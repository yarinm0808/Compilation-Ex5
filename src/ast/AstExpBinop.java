package ast;

import types.*;
import temp.*;
import ir.*;

public class AstExpBinop extends AstExp
{
	int op;
	public AstExp left;
	public AstExp right;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpBinop(AstExp left, AstExp right, int op)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== exp -> exp BINOP exp\n");

		/*******************************/
		/* COPY INPUT DATA MENBERS ... */
		/*******************************/
		this.left = left;
		this.right = right;
		this.op = op;
	}
	
	/*************************************************/
	/* The printing message for a binop exp AST node */
	/*************************************************/
	public void printMe()
	{
		String sop="";
		
		/*********************************/
		/* CONVERT OP to a printable sop */
		/*********************************/
		if (op == 0) {sop = "+";}
		if (op == 1) {sop = "-";}
		if (op == 2) {sop = "*";}
		if (op == 3) {sop = "/";}
		if (op == 6) {sop = "=";}

		/**********************************/
		/* AST NODE TYPE = AST BINOP EXP */
		/*********************************/
		System.out.print("AST NODE BINOP EXP\n");
		System.out.format("BINOP EXP(%s)\n",sop);

		/**************************************/
		/* RECURSIVELY PRINT left + right ... */
		/**************************************/
		if (left != null) left.printMe();
		if (right != null) right.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("BINOP(%s)",sop));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (left  != null) AstGraphviz.getInstance().logEdge(serialNumber,left.serialNumber);
		if (right != null) AstGraphviz.getInstance().logEdge(serialNumber,right.serialNumber);
	}

	public Type semantMe() {
		Type t1 = (left != null)  ? left.semantMe()  : null;
		Type t2 = (right != null) ? right.semantMe() : null;

		// 1. Both are Integers (e.g., 5 + 6 or i < 10)
		if (t1 == TypeInt.getInstance() && t2 == TypeInt.getInstance()) {
			return TypeInt.getInstance();
		}

		// 2. Pointer Equality (e.g., l1 = nil or l1 = l2)
		// We only allow this for the Equality operator (op == 6)
		if (op == 6) {
			// Rule A: Comparing two pointers of the EXACT same class
			if (t1 == t2) return TypeInt.getInstance();

			// Rule B: Comparing any Class pointer to 'nil'
			if ((t1 instanceof TypeClass && t2 instanceof TypeNil) ||
				(t1 instanceof TypeNil && t2 instanceof TypeClass)) {
				return TypeInt.getInstance();
			}
		}

		// If we reach here, it's a real error
		throw new RuntimeException(">> ERROR: Type mismatch in Binary Operation: " + t1 + " and " + t2);
	}

	public Temp irMe()
	{
		Temp t1 = null;
		Temp t2 = null;
		Temp dst = TempFactory.getInstance().getFreshTemp();

		if (left  != null) t1 = left.irMe();
		if (right != null) t2 = right.irMe();

		if (op == 0)
		{
			System.out.println("[DEBUG] op:" + op +" bin operation is ADD");
			Ir.
					getInstance().
					AddIrCommand(new IrCommandBinopAddIntegers(dst,t1,t2));
		}
		if(op == 1){
			System.out.println("[DEBUG] op:" + op +" bin operation is SUB");
			Ir. 	
					getInstance().
					AddIrCommand(new IrCommandBinopSubIntegers(dst, t1, t2));
		}
		if (op == 2)
		{
			System.out.println("[DEBUG] op:" + op +" bin operation is MUL");
			Ir.
					getInstance().
					AddIrCommand(new IrCommandBinopMulIntegers(dst,t1,t2));
		}
		if (op == 3)
    	{
			System.out.println("[DEBUG] op:" + op +" bin operation is DIV");
        	// For integers, it's a value comparison. 
        	// For Classes/Arrays, it should be an address comparison[cite: 24, 27].
			Ir.getInstance().AddIrCommand(new IrCommand_Check_Division_By_Zero(t2));
        	Ir.getInstance().AddIrCommand(new IrCommandBinopDivIntegers(dst, t1, t2));
    	}
		if (op == 4) { 
			System.out.println("[DEBUG] op:" + op +" bin operation is LT");
			Ir.getInstance().AddIrCommand(new IrCommandBinopLtIntegers(dst, t1, t2));
		}
		if (op == 6)
		{
			System.out.println("[DEBUG] op:" + op +" bin operation is EQ");
			Ir.
					getInstance().
					AddIrCommand(new IrCommandBinopEqIntegers(dst,t1,t2));
		}
		return dst;
	}

}
