package ir;

import temp.*;
import mips.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class IrCommandLoadSubscript extends IrCommand {
    Temp base;
    Temp idx;
    Temp res;

    public IrCommandLoadSubscript(Temp res, Temp base, Temp idx) {
        this.res = res;
        this.base = base;
        this.idx = idx;
    }

    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<Temp>();
        // We need the base pointer and the index value to calculate the address
        if (this.base != null) temps.add(this.base);
        if (this.idx != null) temps.add(this.idx);
        return temps;
    }
    
    @Override
    public List<Temp> GetDefTemps() {
        List<Temp> temps = new ArrayList<Temp>();
        // The result of the load goes into this temp
        if (this.res != null) temps.add(this.res);
        return temps;
    }
    
    @Override
    public void mipsMe(Map<Temp, String> tempToReg) {
        String regBase = tempToReg.get(this.base);
        String regIdx = tempToReg.get(this.idx);
        String regRes = tempToReg.get(this.res);

        MipsGenerator mips = MipsGenerator.getInstance();

        // Use a RAW add/addi that does NOT saturate!
        mips.add(String.format("addi $s7, %s, 1",regIdx));    // $at = index + 1
        mips.add(String.format("sll $s7, $s7, 2"));          // $at = (index + 1) * 4
        mips.add(String.format("addu $s7, $s7, %s", regBase)); // $at = Base + Offset

        // Load from calculated address
        mips.add(String.format("lw %s,0($s7)", regRes)); 
    }
}