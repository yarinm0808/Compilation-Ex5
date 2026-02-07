package ir;

import mips.*;
import temp.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map; // Don't forget this import

public class IrCommand_Call extends IrCommand {
    public Temp dst;
    public String funcName;
    public List<Temp> args;

    public IrCommand_Call(Temp dst, String funcName, List<Temp> args) {
        this.dst = dst;
        this.funcName = funcName;
        // Ensure args is never null to make liveness analysis easier
        this.args = (args != null) ? args : new ArrayList<>();
    }

    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        // 1. Map the destination Temp to its physical register (if it exists)
        String physicalDst = (dst != null) ? regMap.get(dst) : null;

        // 2. Map all argument Temps to their physical registers
        List<String> physicalArgs = new ArrayList<>();
        for (Temp arg : args) {
            physicalArgs.add(regMap.get(arg));
        }

        // 3. Delegate to MipsGenerator using physical register names
        MipsGenerator.getInstance().call(physicalDst, funcName, physicalArgs);
    }

    @Override
    public List<Temp> GetUsedTemps() {
        // A call "uses" every temporary passed as an argument
        return new ArrayList<>(this.args);
    }

    @Override
    public List<Temp> GetDefTemps() {
        List<Temp> temps = new ArrayList<>();
        if (this.dst != null) {
            temps.add(this.dst);
        }
        return temps;
    }
}