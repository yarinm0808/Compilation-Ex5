package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import temp.*;
import mips.*;

public class IrCommandCall extends IrCommand {
    public String funcName;
    public Temp dst;        // Can be null for void functions
    public List<Temp> args; // Can be empty/null

    // --- CONSTRUCTOR 1: Existing (Backward Compatible) ---
    public IrCommandCall(String funcName) {
        this.funcName = funcName;
        this.dst = null;
        this.args = new ArrayList<>();
    }

    // --- CONSTRUCTOR 2: New (For String Concat & Methods) ---
    public IrCommandCall(Temp dst, String funcName, List<Temp> args) {
        this.dst = dst;
        this.funcName = funcName;
        this.args = args;
    }

    @Override
    public List<Temp> GetUsedTemps() {
        // If args is null, return empty list to avoid NPE
        return args != null ? args : new ArrayList<>();
    }
    
    @Override
    public List<Temp> GetDefTemps() {
        List<Temp> defs = new ArrayList<>();
        // Only report a 'definition' if we actually have a destination Temp
        if (dst != null) {
            defs.add(dst);
        }
        return defs;
    }
    
    @Override
    public void mipsMe(Map<Temp, String> tempToReg) {
        // For existing void calls (like PrintInt), this remains the same
        MipsGenerator.getInstance().func_call(this.funcName);
        
        // For new calls with a return value (like string_concat)
        if (dst != null) {
            String dstReg = tempToReg.get(dst);
            // You need a method in MipsGenerator to move $v0 to your register
            MipsGenerator.getInstance().move_v0_to_reg(dstReg);
        }
    }
}
