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

public class IRcommandConstInt extends IrCommand
{
	Temp t;
	int value;
	
	public IRcommandConstInt(Temp t, int value)
	{
		this.t = t;
		this.value = value;
	}
	
	/***************/
	/* MIPS me !!! */
	/***************/
    @Override
    public void mipsMe(java.util.Map<Temp, String> regMap) {
        String strT = regMap.get(this.t);
        if (strT != null) {
            MipsGenerator.getInstance().li(strT, value);
        }
    }
	@Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<Temp>();
        return temps;
    }
    @Override
    public List<Temp> GetDefTemps(){
        List<Temp> temps = new ArrayList<Temp>();
		temps.add(this.t);
		return temps;
    }
}
