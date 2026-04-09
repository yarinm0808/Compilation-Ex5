package ir;
import java.util.Map;
import temp.*;
import java.util.ArrayList;
import java.util.List;
import mips.*;

public class IrCommandLoadField extends IrCommand {
    Temp dst;
    Temp baseAddr; // Use the Temp from var.irMe()
    int offset;

    public IrCommandLoadField(Temp dst, Temp baseAddr, int offset) {
        this.dst = dst;
        this.baseAddr = baseAddr;
        this.offset = offset;
    }

    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<>();
        if (this.baseAddr != null) temps.add(this.baseAddr); // This USES the temp, allowing it to "die"
        return temps;
    }

    @Override
    public List<Temp> GetDefTemps() {
        List<Temp> temps = new ArrayList<>();
        if (this.dst != null) temps.add(this.dst);
        return temps;
    }

    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        String d = regMap.get(dst);
        String b = regMap.get(baseAddr);
        // Generates: lw $d, offset($b)
        MipsGenerator.getInstance().load(d, b, offset);
    }


}