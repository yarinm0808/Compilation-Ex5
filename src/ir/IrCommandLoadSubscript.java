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
        temps.add(this.base);
        temps.add(this.idx);
        return temps;
    }
    
    @Override
    public List<Temp> GetDefTemps() {
        List<Temp> temps = new ArrayList<Temp>();
        // The result of the load goes into this temp
        temps.add(this.res);
        return temps;
    }
    
    @Override
    public void mipsMe(Map<Temp, String> tempToReg) {
        String regBase = tempToReg.get(this.base);
        String regIdx = tempToReg.get(this.idx);
        String regRes = tempToReg.get(this.res);

        MipsGenerator mips = MipsGenerator.getInstance();

        // 1. Calculate the offset: (index + 1) * 4
        // We use $at (Assembler Temporary) for intermediate math to avoid overwriting your temps
        mips.add(String.format("addi $at, %s, 1", regIdx)); // $at = index + 1 (skip header)
        mips.add("sll $at, $at, 2");                       // $at = $at * 4 (convert to bytes)

        // 2. Add the offset to the base address
        mips.add(String.format("addu $at, $at, %s", regBase)); // $at = Base + Offset

        // 3. Load the actual value from memory into the result register
        mips.add(String.format("lw %s, 0($at)", regRes));
    }
}