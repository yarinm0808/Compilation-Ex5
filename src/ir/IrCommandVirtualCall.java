package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mips.*;
import temp.Temp;

public class IrCommandVirtualCall extends IrCommand {
    public Temp dst;              // Destination for return value ($v0)
    public Temp addressTemp;      // Register holding the function address from VMT
    public List<Temp> params;     // ALL params (index 0 is 'this', index 1+ are args)

    public IrCommandVirtualCall(Temp dst, Temp addressTemp, List<Temp> params) {
        this.dst = dst;
        this.addressTemp = addressTemp;
        this.params = params;
    }

    @Override
    public void mipsMe(Map<Temp, String> tempToReg) {
        MipsGenerator gen = MipsGenerator.getInstance();
        
        // 1. Push all parameters onto the stack in REVERSE order (Standard MIPS/L calling convention)
        // Note: params.get(0) is 'this', which ends up at 8($fp)
        for (int i = params.size() - 1; i >= 0; i--) {
            String regName = tempToReg.get(params.get(i));
            gen.addiu("$sp", "$sp", -4);
            gen.store(regName, "$sp", 0);
        }

        // 2. Perform dynamic jump
        String addrReg = tempToReg.get(addressTemp);
        gen.add(String.format("\tjalr %s\n", addrReg));

        // 3. Pop all parameters off the stack
        gen.addiu("$sp", "$sp", params.size() * 4);

        // 4. Move result from $v0 to destination temp
        if (this.dst != null) {
            String dstReg = tempToReg.get(this.dst);
            gen.add(String.format("move %s, $v0", dstReg));
        }
    }

    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> used = new ArrayList<>();
        used.add(addressTemp);
        used.addAll(params); // All parameters (including 'this') are used
        return used;
    }

    @Override
    public List<Temp> GetDefTemps() {
        List<Temp> defs = new ArrayList<>();
        if (dst != null) defs.add(dst);
        return defs;
    }
}