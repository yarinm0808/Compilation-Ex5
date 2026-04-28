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

        // Ensure neither side is completely broken
        if (this.leftType == null || this.rightType == null) {
             throw new RuntimeException("ERROR(" + lineNumber + ")");
        }

        // 1. Equality Operator (op == 6)
        if (this.op == 6) {
            // Int == Int
            if (leftType instanceof TypeInt && rightType instanceof TypeInt) return TypeInt.getInstance();
            
            // String == String
            if (leftType instanceof TypeString && rightType instanceof TypeString) return TypeInt.getInstance();
            
            // Nil comparisons (can compare Nil to Class, Array, or another Nil)
            if (leftType instanceof TypeNil && (rightType instanceof TypeClass || rightType instanceof TypeArray || rightType instanceof TypeNil)) return TypeInt.getInstance();
            if (rightType instanceof TypeNil && (leftType instanceof TypeClass || leftType instanceof TypeArray || leftType instanceof TypeNil)) return TypeInt.getInstance();
            
            // Class/Array comparisons 
            // 1. Check if they are literally the same object in memory (handles TypeInt, TypeString singletons)
            if (leftType == rightType) return TypeInt.getInstance();
            
            // 2. Check compatibility (handles Inheritance: A = B where B extends A)
            if (leftType.isCompatible(rightType) || rightType.isCompatible(leftType)) {
                return TypeInt.getInstance();
            }
            
            // If none of the above match, it's an illegal comparison!
            throw new RuntimeException("ERROR(" + lineNumber + ")");
        }

        // 2. Math Operators (op 1-5, and sometimes 0)
        if (leftType instanceof TypeInt && rightType instanceof TypeInt) {
            return TypeInt.getInstance();
        }

        // 3. String Concatenation (op == 0)
        if (leftType instanceof TypeString && rightType instanceof TypeString && this.op == 0) {
            return TypeString.getInstance();
        }

        // 4. Anything that makes it here is a Type Mismatch!
        throw new RuntimeException("ERROR(" + lineNumber + ")");
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
