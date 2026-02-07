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

public class IrCommandLoad extends IrCommand
{
	Temp dst;
	String varName;
	
	public IrCommandLoad(Temp dst, String varName)
	{
		this.dst      = dst;
		this.varName = varName;
	}
	
	/***************/
	/* MIPS me !!! */
	/***************/
	@Override
	public void mipsMe(java.util.Map<Temp, String> regMap)
	{
		String StrDst =regMap.get(this.dst);
		MipsGenerator.getInstance().load(StrDst, varName);
	}
	@Override
    public List<Temp> GetDefTemps() {
        List<Temp> temps = new ArrayList<>();
        temps.add(this.dst);
        return temps;
    }
	
	@Override
	public List<Temp> GetUsedTemps(){
		return new ArrayList<Temp>();
	}
}
