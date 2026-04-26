package ast;

import types.*;
import temp.*;

import java.util.ArrayList;
import java.util.List;

import ir.*;

public class AstExpBinop extends AstExp
{
	int op;
	public AstExp left;
	public AstExp right;
	private Type leftType;
	private Type rightType;
	
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

	@Override
	public Type semantMe() {
		this.leftType = (left != null) ? left.semantMe() : null;
		this.rightType = (right != null) ? right.semantMe() : null;

		// 1. Both are Integers
		if (leftType == TypeInt.getInstance() && rightType == TypeInt.getInstance()) {
			return TypeInt.getInstance();
		}

		// 2. Both are Strings (Concatenation)
		if (leftType == TypeString.getInstance() && rightType == TypeString.getInstance() && this.op == 0) {
			return TypeString.getInstance();
		}

		// 3. Pointer Equality (op == 6)
		if (op == 6) {
			if (leftType == rightType) return TypeInt.getInstance();
			if ((leftType instanceof TypeClass && rightType instanceof TypeNil) ||
				(leftType instanceof TypeNil && rightType instanceof TypeClass)) {
				return TypeInt.getInstance();
			}
		}

		throw new RuntimeException(">> ERROR: Type mismatch in Binary Operation");
	}

	public Temp irMe()
	{
		Temp t1 = null;
		Temp t2 = null;
		Temp dst = TempFactory.getInstance().getFreshTemp();

		if (left  != null) t1 = left.irMe();
		if (right != null) t2 = right.irMe();

		// STRING CONCATENATION LOGIC
		if (leftType == TypeString.getInstance() && rightType == TypeString.getInstance() && op == 0) {
			// We "fake" a call to a MIPS library function called 'string_concat'
			// You should have a command like IrCommand_Call or IrCommand_Function_Call
			Ir.getInstance().AddIrCommand(new IrCommandBinopConcatStrings(dst, t1, t2));
			return dst;
		}

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
		if (op == 5){
			System.out.println("[DEBUG] op:\" + op +\" bin operation is GT");
			Ir.getInstance().AddIrCommand(new IrCommandBinopGtIntegers(dst, t1, t2));
		}
		if (op == 6)
        {
            System.out.println("[DEBUG] op:" + op +" bin operation is EQ");
            
            // Check if we are comparing two Strings
            if (leftType == TypeString.getInstance() && rightType == TypeString.getInstance()) {
                // Use our new byte-by-byte String comparator
                Ir.getInstance().AddIrCommand(new IrCommandBinopEqStrings(dst, t1, t2));
            } else {
                // Otherwise, it's Integers, Arrays, or Objects (which are all just pointer/value comparisons)
                Ir.getInstance().AddIrCommand(new IrCommandBinopEqIntegers(dst, t1, t2));
            }
        }
		return dst;
	}

}
