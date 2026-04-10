package ir;

import temp.Temp;
import mips.MipsGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IrCommandStoreVmt extends IrCommand {
    public Temp objAddr;
    public String vmtLabel;

    public IrCommandStoreVmt(Temp objAddr, String vmtLabel) {
        this.objAddr = objAddr;
        this.vmtLabel = vmtLabel;
    }

    @Override
    public void mipsMe(Map<Temp, String> tempToReg) {
        String reg = tempToReg.get(objAddr);
        MipsGenerator gen = MipsGenerator.getInstance();
        
        // la $at, Son_VMT
        // sw $at, 0($reg)
        gen.add(String.format("\tla $at, %s\n", vmtLabel));
        gen.store("$at", reg, 0);
    }

    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> used = new ArrayList<>();
        used.add(objAddr);
        return used;
    }

    @Override
    public List<Temp> GetDefTemps() {
        return new ArrayList<>(); // Stores don't define new temps
    }
}