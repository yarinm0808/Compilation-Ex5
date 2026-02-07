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

public class IrCommandJumpLabel extends IrCommand
{
	public String labelName;
	
	public IrCommandJumpLabel(String labelName)
	{
		this.labelName = labelName;
	}
	
	/***************/
	/* MIPS me !!! */
	/***************/
	public void mipsMe(java.util.Map<Temp, String> regMap)
	{
		MipsGenerator.getInstance().jump(labelName);
	}

	@Override
	public List<Temp> GetUsedTemps()
	{
		return new ArrayList<Temp>();
	}

	@Override
	public List<Temp> GetDefTemps()
	{
		return new ArrayList<Temp>();
	}
}
