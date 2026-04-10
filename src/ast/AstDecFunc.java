package ast;

import types.*;
import symboltable.*;
import temp.*;
import ir.*;

public class AstDecFunc extends AstDec {
    /****************/
    /* DATA MEMBERS */
    /****************/
    public AstType returnTypeNode; 
    public String name;
    public AstTypeNameList params;
    public AstStmtList body;
	public String className = null;

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
        // [0] Resolve return type
        Type returnType = (returnTypeNode != null) ? returnTypeNode.semantMe() : TypeVoid.getInstance();

        // [1] Build the parameter type list IN ORDER
        TypeList head = null;
        TypeList tail = null;
        for (AstTypeNameList it = params; it != null; it = it.tail) {
            Type t = it.head.semantMe();
            TypeList newNode = new TypeList(t, null);
            if (head == null) {
                head = newNode;
                tail = newNode;
            } else {
                tail.tail = newNode;
                tail = newNode;
            }
        }

        // Create signature 
        TypeFunction funcSignature = new TypeFunction(returnType, name, head);
		funcSignature.definingClassName = this.className;

        // --- INHERITANCE / OVERRIDE FIX ---
        // We only throw an error if the name is already defined as something 
        // OTHER than a function (like a field) or if it's a global function conflict.
        Type existingType = SymbolTable.getInstance().findInCurrentScope(name);
		if (existingType != null) {
			// Redefinition Rule:
			// If we find something in the current scope, we only allow it if it is 
			// a function (an override). If it is a field or variable, it's an error.
			if (!(existingType instanceof TypeFunction)) {
				throw new RuntimeException("ERROR(" + lineNumber + "): name " + name + " already defined.");
			}
		}
        
        // Enter signature into scope (Global or Class scope)
        SymbolTable.getInstance().enter(name, funcSignature);

        // [2] Open Function Scope
        SymbolTable.getInstance().beginScope();
        SymbolTable.getInstance().resetLocalVarIndex();

        // [3] Populate parameters and save their Entries
        // Standalone functions: Param 0 at 8($fp)
        // Class methods: 'this' at 8($fp), Param 0 at 12($fp)
        int currentParamOffset = (this.className != null) ? 12 : 8;

        for (AstTypeNameList it = params; it != null; it = it.tail) {
            Type t = it.head.semantMe();
            it.head.entry = SymbolTable.getInstance().enter(it.head.name, t);
            it.head.entry.isParameter = true;
            it.head.entry.setOffset(currentParamOffset);
            currentParamOffset += 4;
        }

        // [4] Analyze Body
        if (body != null) {
            SymbolTable.getInstance().currentExpectedReturnType = returnType;
            body.semantMe();
            SymbolTable.getInstance().currentExpectedReturnType = null;
        }

        // [5] Close Scope
        SymbolTable.getInstance().endScope();
        
        return funcSignature;
    }
	
	@Override
	public Temp irMe() {
		// 1. Determine the Labels (Name Mangling)
		// If className is set, it's a method: Student_getAverage
		// Otherwise, it's a global function: func_monthJuly
		String labelBase  = (className != null) ? className + "_" + name : "func_" + name;
		String entryLabel = labelBase;
		String exitLabel  = "end_" + labelBase;

		// Set the exit label in context so 'return' statements know where to jump
		ControlFlowContext.getInstance().setCurrentFunctionEndLabel(exitLabel);

		// 2. Setup Parameter Offsets
		// In MIPS: $fp+4 is $ra, $fp+0 is old $fp.
		// The first argument starts at +8.
		int currentParamOffset = 8; 

		if (className != null) {
			// [HIDDEN PARAMETER]
			// If this is a method, the pointer to the object ('this') 
			// is implicitly passed as the first argument at +8.
			System.out.println(">> [IR] Method " + name + ": Reserved +8 for 'this'");
			currentParamOffset = 12; // User parameters start at +12
		}

		for (AstTypeNameList it = params; it != null; it = it.tail) {
			// Map the parameter name to its stack offset relative to $fp
			it.head.entry.setOffset(currentParamOffset);
			it.head.entry.isParameter = true; 
			System.out.println(">> [IR] Param: " + it.head.name + " at +" + currentParamOffset);
			currentParamOffset += 4;
		}

		// 3. Prepare for Local Variables
		// Reset the counter so locals in THIS function start at -4, -8, etc.
		StackOffsetManager.getInstance().reset(0); 

		// 4. Capture the Body IR
		// We swap the global IR head/tail so we only capture the body commands 
		// without the prologue (since we don't know the stack size yet).
		Ir globalIr = Ir.getInstance();
		IrCommand oldHead = globalIr.head;
		IrCommandList oldTail = globalIr.tail;
		globalIr.head = null;
		globalIr.tail = null;

		if (body != null) {
			body.irMe();
		}

		// Store the body commands we just generated
		IrCommand bodyHead = globalIr.head;
		IrCommandList bodyTail = globalIr.tail;

		// Restore the global IR state
		globalIr.head = oldHead;
		globalIr.tail = oldTail;

		// 5. Final Stack Calculation
		// Now that body.irMe() has run, we know how many locals were declared.
		int totalLocals = StackOffsetManager.getInstance().getCount();
		int bytesNeeded = totalLocals * 4;

		// 6. Assemble the Final Function Block
		// Order: Label -> Prologue -> Body -> Exit Label -> Epilogue -> jr $ra
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

		return null; // Functions don't return a Temp during declaration
	}
}