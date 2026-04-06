package ast;

import types.*;
import symboltable.*;
import temp.*;
import ir.*;

public class AstDecFunc extends AstDec {
    /****************/
    /* DATA MEMBERS */
    /****************/
    // CHANGE: Use AstType instead of String
    public AstType returnTypeNode; 
    public String name;
    public AstTypeNameList params;
    public AstStmtList body;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstDecFunc(
        AstType returnTypeNode, // CHANGE: Accept the node
        String name,
        AstTypeNameList params,
        AstStmtList body)
    {
        serialNumber = AstNodeSerialNumber.getFresh();

        this.returnTypeNode = returnTypeNode; // Store the node
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
		System.out.format("FUNC(%s):%s\n",name,returnTypeNode);

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
			String.format("FUNC(%s)\n:%s\n",name,returnTypeNode));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (params != null) AstGraphviz.getInstance().logEdge(serialNumber,params.serialNumber);
		if (body   != null) AstGraphviz.getInstance().logEdge(serialNumber,body.serialNumber);
	}

	@Override
	public Type semantMe() {
		// [0] Resolve return type node
		Type returnType = returnTypeNode.semantMe();
		if (returnType == null) returnType = TypeVoid.getInstance();

		// [2] Build the parameter type list
		TypeList type_list = null;
		for (AstTypeNameList it = params; it != null; it = it.tail) {
			// CHANGE: Use it.head.semantMe() instead of manual find()
			// it.head is an AstTypeName, which now knows how to resolve its typeNode
			Type t = it.head.semantMe(); 
			
			type_list = new TypeList(t, type_list);
		}

		// [3] Register Function EARLY
		SymbolTable.getInstance().enter(name, new TypeFunction(returnType, name, type_list));

		// [4] Analyze Body in a new scope
		SymbolTable.getInstance().beginScope();

		// [5] Populate the scope with parameters
		for (AstTypeNameList it = params; it != null; it = it.tail) {
			// CHANGE: Again, use the node's semantMe
			Type t = it.head.semantMe();
			
			// it.head.entry was already set inside it.head.semantMe()!
			// No need to call enter() again here unless you want to be extra safe
		}

		if (body != null) body.semantMe();

		SymbolTable.getInstance().endScope();
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