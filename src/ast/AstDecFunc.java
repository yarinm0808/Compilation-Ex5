package ast;

import types.*;
import symboltable.*;
import temp.*;
import ir.*;

public class AstDecFunc extends AstDec
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public String returnTypeName;
	public String name;
	public AstTypeNameList params;
	public AstStmtList body;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstDecFunc(
		String returnTypeName,
		String name,
		AstTypeNameList params,
		AstStmtList body)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.returnTypeName = returnTypeName;
		this.name = name;
		this.params = params;
		this.body = body;
	}

	/************************************************************/
	/* The printing message for a function declaration AST node */
	/************************************************************/
	public void printMe()
	{
		/*************************************************/
		/* AST NODE TYPE = AST NODE FUNCTION DECLARATION */
		/*************************************************/
		System.out.format("FUNC(%s):%s\n",name,returnTypeName);

		/***************************************/
		/* RECURSIVELY PRINT params + body ... */
		/***************************************/
		if (params != null) params.printMe();
		if (body   != null) body.printMe();
		
		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("FUNC(%s)\n:%s\n",name,returnTypeName));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (params != null) AstGraphviz.getInstance().logEdge(serialNumber,params.serialNumber);
		if (body   != null) AstGraphviz.getInstance().logEdge(serialNumber,body.serialNumber);
	}

	public Type semantMe()
	{
		Type t;
		Type returnType = null;
		TypeList type_list = null;

		/*******************/
		/* [0] return type */
		/*******************/
		returnType = SymbolTable.getInstance().find(returnTypeName);
		if (returnType == null)
		{
			System.out.format(">> ERROR [%d:%d] non existing return type %s\n",6,6,returnType);				
		}
	
		/****************************/
		/* [1] Begin Function Scope */
		/****************************/
		SymbolTable.getInstance().beginScope();

		/***************************/
		/* [2] Semant Input Params */
		/***************************/
		for (AstTypeNameList it = params; it  != null; it = it.tail)
		{
			t = SymbolTable.getInstance().find(it.head.type);
			if (t == null)
			{
				System.out.format(">> ERROR [%d:%d] non existing type %s\n",2,2,it.head.type);				
			}
			else
			{
				type_list = new TypeList(t,type_list);
				it.head.entry = SymbolTable.getInstance().enter(it.head.name, t);
			}
		}

		/*******************/
		/* [3] Semant Body */
		/*******************/
		body.semantMe();

		/*****************/
		/* [4] End Scope */
		/*****************/
		SymbolTable.getInstance().endScope();

		/***************************************************/
		/* [5] Enter the Function Type to the Symbol Table */
		/***************************************************/
		SymbolTable.getInstance().enter(name,new TypeFunction(returnType,name,type_list));

		/************************************************************/
		/* [6] Return value is irrelevant for function declarations */
		/************************************************************/
		return null;		
	}

	
	public Temp irMe() {
		String entryLabel = "func_" + name;
		String exitLabel  = "end_" + name;

		ControlFlowContext.getInstance().setCurrentFunctionEndLabel(exitLabel);

		// [1] Setup Parameters (Positive Offsets)
		int currentParamOffset = 8; 
		for (AstTypeNameList it = params; it != null; it = it.tail) {
			it.head.entry.setOffset(currentParamOffset);
			it.head.entry.isParameter = true; // IMPORTANT
			System.out.println("[DEBUG] Param: " + it.head.name + " marked at +" + currentParamOffset);
			currentParamOffset += 4;
		}

		// [2] Reset Local Counter
		StackOffsetManager.getInstance().reset(0); 

		// [3] Buffer Body Commands
		Ir globalIr = Ir.getInstance();
		IrCommand realHead = globalIr.head;
		IrCommandList realTail = globalIr.tail;
		globalIr.head = null;
		globalIr.tail = null;

		if (body != null) body.irMe();

		IrCommand bodyHead = globalIr.head;
		IrCommandList bodyTail = globalIr.tail;
		globalIr.head = realHead;
		globalIr.tail = realTail;

		// [4] Final Measurement
		int totalLocals = StackOffsetManager.getInstance().getCount();
		int bytesNeeded = totalLocals * 4;
		System.out.println("[DEBUG] Function " + name + " stack space for locals: " + bytesNeeded);

		// [5] Final Sequence
		globalIr.AddIrCommand(new IrCommandLabel(entryLabel));
		globalIr.AddIrCommand(new IrCommandPrologue(bytesNeeded));
		
		if (bodyHead != null) {
			globalIr.AddIrCommand(bodyHead);
			IrCommandList curr = bodyTail;
			while (curr != null) {
				globalIr.AddIrCommand(curr.head);
				curr = curr.tail;
			}
		}

		globalIr.AddIrCommand(new IrCommandLabel(exitLabel));
		globalIr.AddIrCommand(new IrCommandEpilogue(bytesNeeded));
		globalIr.AddIrCommand(new IrCommandJumpToRa());

		return null;
	}
}