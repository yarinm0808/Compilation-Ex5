package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import temp.Temp;
import mips.*;

public class IrCommandStoreField extends IrCommand {
    Temp baseAddr; // The address of 'p'
    int offset;    // The offset of 'age' (0)
    Temp value;    // The value to store (5)

    public IrCommandStoreField(Temp baseAddr, int offset, Temp value) {
        this.baseAddr = baseAddr;
        this.offset = offset;
        this.value = value;
    }

    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        String b = regMap.get(baseAddr); // e.g., "$t1"
        String v = regMap.get(value);    // e.g., "$t0"
        
        // This generates: sw $t0, 0($t1)
        MipsGenerator.getInstance().storeField(v, b, offset);
    }


    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<Temp>();
        if (this.baseAddr != null) temps.add(this.baseAddr);
        if (this.value != null) temps.add(this.value);
        return temps;
    }

    @Override
    public List<Temp> GetDefTemps() {
        return new ArrayList<Temp>();
    }
}
