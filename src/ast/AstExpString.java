package ast;

import ir.Ir;
import ir.IrCommandAllocateString;
import ir.IrCommandLoad;
import mips.MipsGenerator;
import temp.Temp;
import temp.TempFactory;
import types.*;

public class AstExpString extends AstExp
{
	public String value;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpString(String value)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		System.out.format("====================== exp -> STRING( %s )\n", value);
		this.value = value;
	}

	/******************************************************/
	/* The printing message for a STRING EXP AST node */
	/******************************************************/
	public void printMe()
	{
		/*******************************/
		/* AST NODE TYPE = AST STRING EXP */
		/*******************************/
		System.out.format("AST NODE STRING( %s )\n",value);

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("STRING\n%s",value.replace('"','\'')));
	}

	public Type semantMe()
	{
		return TypeString.getInstance();
	}

	@Override
    public Temp irMe() {
        // 1. Generate a unique label for this specific literal
        String label = "string_lit_" + serialNumber;

        // 2. Clean the string. Lexers often include the surrounding quotes.
        // Since your allocateString adds escaped quotes (\"), we strip them here
        // to avoid getting ""Having"" in MIPS.
        String cleanValue = value;
        if (cleanValue.startsWith("\"") && cleanValue.endsWith("\"")) {
            cleanValue = cleanValue.substring(1, cleanValue.length() - 1);
        }

        Ir.getInstance().AddIrCommand(new IrCommandAllocateString(label, cleanValue));

        // 4. Create a Temp and issue a definition command
        // This is the "Define" step. Because of this, the allocator 
        // will give 't' a real register like $t0 instead of 'null'.
        Temp t = TempFactory.getInstance().getFreshTemp();
        Ir.getInstance().AddIrCommand(new IrCommandLoad(t, label, 0, true));

        return t;
    }
}
