package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import temp.Temp;
import mips.*;

public class IrCommandAllocateString extends IrCommand {
    public String name;
    public String strVal;

    public IrCommandAllocateString(String name, String strVal) {
        this.name = name;
        this.strVal = strVal;
    }

    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        MipsGenerator mips = MipsGenerator.getInstance();
        
        // 1. Create a unique label for the actual characters
        String strLabel ="global_" + name + "_str_lit";
        
        // 2. Emit the null-terminated string
        mips.addnotab(strLabel + ": .asciiz \"" + strVal + "\"");
        
        // 3. Ensure the next word is aligned correctly
        mips.addnotab(".align 2");
        
        // 4. Emit the pointer variable that the program actually uses
        mips.addnotab("global_" + name + ": .word " + strLabel);
    }

    @Override
    public List<Temp> GetUsedTemps() { return new ArrayList<Temp>(); }
    @Override
    public List<Temp> GetDefTemps() { return new ArrayList<Temp>(); }
}