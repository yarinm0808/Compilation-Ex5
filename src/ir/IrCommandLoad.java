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
	int offset;
	
	public IrCommandLoad(Temp dst, String varName, int offset)
	{
		this.dst      = dst;
		this.varName = varName;
		this.offset = offset;
	}
	
	/***************/
	/* MIPS me !!! */
	/***************/
	@Override
	public void mipsMe(java.util.Map<Temp, String> regMap)
	{
		String StrDst =regMap.get(this.dst);
		MipsGenerator.getInstance().load(StrDst, varName, offset);
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
