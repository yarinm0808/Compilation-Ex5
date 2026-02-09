package ast;

import temp.*;
import ir.*;
import types.*;

public class AstStmtWhile extends AstStmt
{
	public AstExp cond;
	public AstStmtList body;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtWhile(AstExp cond, AstStmtList body)
	{
		this.cond = cond;
		this.body = body;
	}

	@Override
	public Type semantMe(){
		Type t = cond.semantMe();
        if (!(t instanceof TypeInt)) {
            throw new RuntimeException("ERROR(" + lineNumber + ")");
        }
        if (body != null) body.semantMe();
		return null;
	}

	public Temp irMe()
	{
		/*******************************/
		/* [1] Allocate 2 fresh labels */
		/*******************************/
		String labelEnd   = IrCommand.getFreshLabel("end");
		String labelStart = IrCommand.getFreshLabel("start");

		/*********************************/
		/* [2] entry label for the while */
		/*********************************/
		Ir.
				getInstance().
				AddIrCommand(new IrCommandLabel(labelStart));

		/********************/
		/* [3] cond.IRme(); */
		/********************/
		Temp condTemp = cond.irMe();

		/******************************************/
		/* [4] Jump conditionally to the loop end */
		/******************************************/
		Ir.
				getInstance().
				AddIrCommand(new IrCommandJumpIfEqToZero(condTemp,labelEnd));

		/*******************/
		/* [5] body.IRme() */
		/*******************/
		body.irMe();

		/******************************/
		/* [6] Jump to the loop entry */
		/******************************/
		Ir.
				getInstance().
				AddIrCommand(new IrCommandJump(labelStart));

		/**********************/
		/* [7] Loop end label */
		/**********************/
		Ir.
				getInstance().
				AddIrCommand(new IrCommandLabel(labelEnd));

		/*******************/
		/* [8] return null */
		/*******************/
		return null;
	}
}