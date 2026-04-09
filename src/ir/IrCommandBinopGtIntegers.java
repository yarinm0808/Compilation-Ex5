package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mips.MipsGenerator;
import temp.Temp;

public class IrCommandBinopGtIntegers extends IrCommand {
    public Temp t1;
    public Temp t2;
    public Temp dst;

    public IrCommandBinopGtIntegers(Temp dst, Temp t1, Temp t2)
    {
        this.dst = dst;
        this.t1 = t1;
        this.t2 = t2;
    }
    
    /***************/
    /* MIPS me !!! */
    /***************/
    @Override
    public void mipsMe(Map<Temp, String> regMap)
    {
        String s1 = regMap.get(this.t1);
        String s2 = regMap.get(this.t2);
        String d  = regMap.get(this.dst);

        // One single instruction replaces all the branching logic!
        MipsGenerator.getInstance().slt(d, s2, s1);
    }
    
    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<Temp>();
        if (this.t1 != null) temps.add(this.t1);
        if (this.t2 != null) temps.add(this.t2);
        return temps;
    }

    @Override
    public List<Temp> GetDefTemps(){
        List<Temp> temps = new ArrayList<Temp>();
        if (this.dst != null) temps.add(this.dst);
        return temps;
    }
}
