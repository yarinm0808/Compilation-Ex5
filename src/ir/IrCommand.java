/***********/
/* PACKAGE */
/***********/
package ir;

import java.util.List;
import java.util.Map;

import temp.*;
/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/

public abstract class IrCommand
{
	/*****************/
	/* Label Factory */
	/*****************/
	protected static int label_counter=0;
	public    static String getFreshLabel(String msg)
	{
		return String.format("Label_%d_%s",label_counter++,msg);
	}

	/***************/
	/* MIPS me !!! */
	/***************/
	public abstract void mipsMe(Map<Temp, String> regMap);

	public abstract List<Temp> GetUsedTemps();
	public abstract List<Temp> GetDefTemps();
}
