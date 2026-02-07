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

public class IrCommandPrintInt extends IrCommand
{
	Temp t;
	
	public IrCommandPrintInt(Temp t)
	{
		this.t = t;
	}
	
	/***************/
	/* MIPS me !!! */
	/***************/
	@Override
	public void mipsMe(java.util.Map<Temp, String> regMap)
	{
		String StrT =regMap.get(this.t);
		MipsGenerator.getInstance().printInt(StrT);
	}
	
	@Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<>();
        temps.add(this.t);
        return temps;
    }

	@Override
	public List<Temp> GetDefTemps(){
		return new ArrayList<Temp>();
	}
}
