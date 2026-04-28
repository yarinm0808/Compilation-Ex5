/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import temp.*;
import mips.*;

public class IrCommandStore extends IrCommand
{
    String varName;
    Temp src;
    int offset;
    boolean isGlobal; // <-- ADDED FLAG
    
    // Updated constructor to accept the isGlobal flag
    public IrCommandStore(String varName, Temp src, int offset, boolean isGlobal)
    {
        this.src     = src;
        this.varName = varName;
        this.offset  = offset;
        this.isGlobal = isGlobal;
    }
    
    /***************/
    /* MIPS me !!! */
    /***************/
    @Override
    public void mipsMe(Map<Temp, String> regMap)
    {
        if (regMap == null) {
            return; 
        }
        
        String StrSrc = regMap.get(this.src);
        
        // If it is a global variable, prefix the name with "global_"
        // so it matches the label in the .data section
        String targetName = this.varName;
        if (this.isGlobal && targetName != null) {
            targetName = "global_" + targetName;
        }

        // Pass the properly prefixed name down to the MipsGenerator
        MipsGenerator.getInstance().store(StrSrc, targetName, offset);
    }

    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<>();
        if (this.src != null) temps.add(this.src);
        return temps;
    }

    @Override
    public List<Temp> GetDefTemps(){
        return new ArrayList<Temp>();
    }
}