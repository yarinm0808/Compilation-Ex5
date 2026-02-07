package ir;

import temp.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map; // Required for the regMap
import mips.*;
import ast.*;
public class IrCommandBinopEqIntegers extends IrCommand
{
    public Temp t1;
    public Temp t2;
    public Temp dst;

    public IrCommandBinopEqIntegers(Temp dst, Temp t1, Temp t2)
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
        // 1. Map virtual Temps to physical register strings ($t0, etc.)
        String s1 = regMap.get(this.t1);
        String s2 = regMap.get(this.t2);
        String d  = regMap.get(this.dst);

        // 2. Generate unique labels
        String labelAssignOne  = "label_assign_one_" + AstNodeSerialNumber.getFresh();
        String labelEnd        = "label_eq_end_" + AstNodeSerialNumber.getFresh();
        
        // 3. Optimized Logic:
        // if (s1 == s2) goto AssignOne
        MipsGenerator.getInstance().beq(s1, s2, labelAssignOne);
        
        // Fall-through: Assign Zero
        MipsGenerator.getInstance().li(d, 0);
        MipsGenerator.getInstance().jump(labelEnd);

        // Label: Assign One
        MipsGenerator.getInstance().label(labelAssignOne);
        MipsGenerator.getInstance().li(d, 1);

        // Label: End
        MipsGenerator.getInstance().label(labelEnd);
    }

    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<Temp>();
        temps.add(this.t1);
        temps.add(this.t2);
        return temps;
    }

    @Override
    public List<Temp> GetDefTemps(){
        List<Temp> temps = new ArrayList<Temp>();
        temps.add(this.dst);
        return temps;
    }
}