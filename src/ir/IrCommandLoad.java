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

public class IrCommandLoad extends IrCommand
{
    Temp dst;
    String varName;
    int offset;
    boolean isGlobal; // <-- The essential flag
    
    public IrCommandLoad(Temp dst, String varName, int offset, boolean isGlobal)
    {
        this.dst      = dst;
        this.varName  = varName;
        this.offset   = offset;
        this.isGlobal = isGlobal;
    }
    
    /***************/
    /* MIPS me !!! */
    /***************/
    @Override
    public void mipsMe(Map<Temp, String> regMap)
    {
        if (regMap == null) return;

        String StrDst = regMap.get(this.dst);
        String targetName = this.varName;

        // ONLY prepend "global_" if it's actually a global variable
        // and the name isn't null.
        if (this.isGlobal && targetName != null) {
            targetName = "global_" + targetName;
        }

        MipsGenerator.getInstance().load(StrDst, targetName, offset);
    }

    @Override
    public List<Temp> GetDefTemps() {
        List<Temp> temps = new ArrayList<>();
        if (this.dst != null) temps.add(this.dst);
        return temps;
    }
    
    @Override
    public List<Temp> GetUsedTemps(){
        return new ArrayList<Temp>();
    }
}