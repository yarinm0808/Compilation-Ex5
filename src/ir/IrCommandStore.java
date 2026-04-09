/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/
import temp.*;

import java.util.ArrayList;
import java.util.List;

import mips.*;

public class IrCommandStore extends IrCommand
{
	String varName;
	Temp src;
	int offset;
	
	public IrCommandStore(String varName, Temp src, int offset)
	{
		this.src      = src;
		this.varName = varName;
		this.offset = offset;
	}
	
	/***************/
	/* MIPS me !!! */
	/***************/
	@Override
	public void mipsMe(java.util.Map<Temp, String> regMap)
	{
		if (regMap == null) {
        // If we are in the global scope, we don't 'store' via registers.
        // The allocation command already set the value in .data.
        return; 
    }
		String StrSrc = regMap.get(this.src);
		// Correct order: Register first, then Name, then Offset
		MipsGenerator.getInstance().store(StrSrc, varName, offset);
	}

	@Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<>();
        if (this.src != null) temps.add(this.src);
        return temps;
    }

	@Override
	public List<Temp> GetDefTemps(){
		return new ArrayList<Temp>();
	}
}
