package ir;

import temp.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mips.*;

public class IrCommandStoreSubscript extends IrCommand {
    public Temp base;
    public Temp idx;
    public Temp val;

    public IrCommandStoreSubscript(Temp base, Temp idx, Temp val) {
        this.base = base;
        this.idx  = idx;
        this.val  = val;
    }

    @Override
    public void mipsMe(Map<Temp, String> tempToReg) {
        String regBase = tempToReg.get(this.base);
        String regIdx  = tempToReg.get(this.idx);
        String regVal  = tempToReg.get(this.val);

        MipsGenerator mips = MipsGenerator.getInstance();

        // 1. Calculate the offset: (index + 1) * 4
        mips.add(String.format("addi $at, %s, 1", regIdx)); 
        mips.add("sll $at, $at, 2"); 

        // 2. Add the offset to the base address
        mips.add(String.format("addu $at, $at, %s", regBase)); 

        // 3. Store the value into the calculated address
        mips.add(String.format("sw %s, 0($at)", regVal));
    }

    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<Temp>();
        // FIX: You MUST add 'base' here!
        temps.add(this.base); 
        temps.add(this.idx);
        temps.add(this.val);
        return temps;
    }

    @Override
    public List<Temp> GetDefTemps() {
        // Correct: Store commands do not 'define' a new temporary value; 
        // they only modify memory.
        return new ArrayList<Temp>();
    }
}