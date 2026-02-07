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
	
	public IrCommandStore(String varName, Temp src)
	{
		this.src      = src;
		this.varName = varName;
	}
	
	/***************/
	/* MIPS me !!! */
	/***************/
	@Override
	public void mipsMe(java.util.Map<Temp, String> regMap)
	{
		String StrSrc = regMap.get(this.src);
		MipsGenerator.getInstance().store(varName,StrSrc);
	}

	@Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<>();
        temps.add(this.src);
        return temps;
    }

	@Override
	public List<Temp> GetDefTemps(){
		return new ArrayList<Temp>();
	}
}
