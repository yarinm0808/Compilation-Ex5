package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map; // Add this import

import mips.*;
import temp.Temp;

public class IrCommandLabel extends IrCommand
{
    public String labelName;
    
    public IrCommandLabel(String labelName)
    {
        this.labelName = labelName;
    }
    
    /***************/
    /* MIPS me !!! */
    /***************/
    @Override
    public void mipsMe(Map<Temp, String> regMap)
    {
        // Labels don't use any registers, so we just pass the name
        MipsGenerator.getInstance().label(labelName);
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