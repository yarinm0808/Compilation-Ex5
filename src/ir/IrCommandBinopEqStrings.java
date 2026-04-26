package ir;

import temp.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mips.*;
import ast.*;

public class IrCommandBinopEqStrings extends IrCommand
{
    public Temp t1;
    public Temp t2;
    public Temp dst;

    public IrCommandBinopEqStrings(Temp dst, Temp t1, Temp t2)
    {
        this.dst = dst;
        this.t1 = t1;
        this.t2 = t2;
    }
    
    @Override
    public void mipsMe(Map<Temp, String> regMap)
    {
        String s1 = regMap.get(this.t1);
        String s2 = regMap.get(this.t2);
        String d  = regMap.get(this.dst);

        int id = AstNodeSerialNumber.getFresh();
        String labelLoop = "label_strcmp_loop_" + id;
        String labelDiff = "label_strcmp_diff_" + id;
        String labelSame = "label_strcmp_same_" + id;
        String labelEnd  = "label_strcmp_end_" + id;
        
        MipsGenerator mips = MipsGenerator.getInstance();
        
        // 1. Copy pointers to safe temporary registers ($a0, $a1)
        mips.add("move $a0, " + s1);
        mips.add("move $a1, " + s2);

        // 2. The Byte-by-Byte Loop
        mips.label(labelLoop); 
        mips.add("lb $v0, 0($a0)"); // Load character from string 1
        mips.add("lb $v1, 0($a1)"); // Load character from string 2
        
        // If characters are different, jump to Diff
        mips.add("bne $v0, $v1, " + labelDiff);
        
        // If they are the same, check if we hit the end of the string (\0)
        // If $v0 is 0, we reached the end and they matched perfectly!
        mips.add("beqz $v0, " + labelSame);
        
        // Otherwise, advance pointers by 1 byte and loop
        mips.add("addiu $a0, $a0, 1");
        mips.add("addiu $a1, $a1, 1");
        mips.jump(labelLoop);

        // 3. Block: Not Equal
        mips.label(labelDiff);
        mips.li(d, 0);
        mips.jump(labelEnd);

        // 4. Block: Equal
        mips.label(labelSame);
        mips.li(d, 1);

        // 5. Block: End
        mips.label(labelEnd);
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