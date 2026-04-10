package ir;

import temp.Temp;
import mips.MipsGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IrCommandLoadFromRegister extends IrCommand {
    public Temp dst;
    public Temp src;
    public int offset;

    public IrCommandLoadFromRegister(Temp dst, Temp src, int offset) {
        this.dst = dst;
        this.src = src;
        this.offset = offset;
    }

    @Override
    public void mipsMe(Map<Temp, String> tempToReg) {
        String dstReg = tempToReg.get(dst);
        String srcReg = tempToReg.get(src);
        
        // Generates: lw $dstReg, offset($srcReg)
        MipsGenerator.getInstance().load(dstReg, srcReg, offset);
    }

    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> used = new ArrayList<>();
        used.add(src); // We use the base address register
        return used;
    }

    @Override
    public List<Temp> GetDefTemps() {
        List<Temp> defs = new ArrayList<>();
        defs.add(dst); // We define the destination register
        return defs;
    }
}