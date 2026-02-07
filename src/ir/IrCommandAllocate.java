/***********/
/* PACKAGE */
/***********/
package ir;

import java.util.ArrayList;
import java.util.List;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/
import mips.*;
import temp.Temp;

public class IrCommandAllocate extends IrCommand
{
	String varName;
	
	public IrCommandAllocate(String varName)
	{
		this.varName = varName;
	}
	
	/***************/
	/* MIPS me !!! */
	/***************/
	public void mipsMe(java.util.Map<Temp, String> regMap)
	{
		MipsGenerator.getInstance().allocate(varName);
	}
	    @Override
    public List<Temp> GetUsedTemps() {
        return null;
    }
    @Override
    public List<Temp> GetDefTemps() {
        return new ArrayList<Temp>();
    }
}
