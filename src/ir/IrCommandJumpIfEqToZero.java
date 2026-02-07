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

public class IrCommandJumpIfEqToZero extends IrCommand
{
	Temp t;
	public String labelName;
	
	public IrCommandJumpIfEqToZero(Temp t, String labelName)
	{
		this.t          = t;
		this.labelName = labelName;
	}
	
	/***************/
	/* MIPS me !!! */
	/***************/
    @Override
    public void mipsMe(java.util.Map<Temp, String> regMap) {
        String strT = regMap.get(this.t);
        if (strT != null) {
            MipsGenerator.getInstance().beqz(strT,this.labelName);
        }
    }
	@Override
	public List<Temp> GetUsedTemps(){
        List<Temp> temps = new ArrayList<Temp>();
        temps.add(this.t);
        return temps;
    }

	@Override
	public List<Temp> GetDefTemps(){
		return new ArrayList<Temp>();
	}
}
