package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import temp.Temp;
import mips.MipsGenerator;

public class IrCommandMoveToA0 extends IrCommand {
    public Temp src;

    public IrCommandMoveToA0(Temp src) {
        this.src = src;
    }

    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        // Look up which physical $t register the allocator gave to this Temp
        String srcRegName = regMap.get(src);
        
        // Emit the MIPS: move $a0, $tX
        MipsGenerator.getInstance().moveA0(srcRegName);
    }

    @Override
    public List<Temp> GetUsedTemps() {
        // We are reading from this Temp, so it's "Used"
        List<Temp> temps = new ArrayList<>();
        temps.add(src);
        return temps;
    }

    @Override
    public List<Temp> GetDefTemps() {
        // We aren't writing to any Temps
        return new ArrayList<>();
    }
}