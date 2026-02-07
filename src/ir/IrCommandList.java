/***********/
/* PACKAGE */
/***********/
package ir;

import java.util.Map;

import temp.Temp;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/

public class IrCommandList
{
	public IrCommand head;
	public IrCommandList tail;

	IrCommandList(IrCommand head, IrCommandList tail)
	{
		this.head = head;
		this.tail = tail;
	}

	/***************/
	/* MIPS me !!! */
	/***************/
	public void mipsMe(Map<Temp, String> regMap)
	{
		if (head != null) head.mipsMe(regMap);
		if (tail != null) tail.mipsMe(regMap);
	}
}
