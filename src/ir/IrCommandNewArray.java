package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mips.*;
import temp.*;
public class IrCommandNewArray extends IrCommand{
    public Temp dst;
    public Temp length;
    public IrCommandNewArray(Temp dst, Temp length){
        this.dst = dst;
        this.length = length;
    }

    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        String regDst = regMap.get(this.dst);
        String regLen = regMap.get(this.length);

        // 1. Move the length into $a0 (argument for sbrk)
        MipsGenerator.getInstance().add(String.format("move $a0, %s", regLen));

        // 2. Add 1 for the header word
        MipsGenerator.getInstance().add("addi $a0, $a0, 1");

        // 3. Multiply by 4 (Shift Left Logical 2 bits) to get byte count
        MipsGenerator.getInstance().add("sll $a0, $a0, 2");

        // 4. System Call 9 is 'sbrk' (allocate heap memory)
        MipsGenerator.getInstance().add("li $v0, 9");
        MipsGenerator.getInstance().add("syscall");

        // 5. $v0 now holds the address of the first byte. 
        // Store the original length (from regLen) into that first word.
        MipsGenerator.getInstance().add(String.format("sw %s, 0($v0)", regLen));

        // 6. Move the pointer from $v0 to our destination register
        MipsGenerator.getInstance().add(String.format("move %s, $v0", regDst));
    }
    @Override
    public List<Temp> GetUsedTemps() {
        ArrayList<Temp> res = new ArrayList<Temp>();
        res.add(this.length);
        return res;
    }

    @Override
    public List<Temp> GetDefTemps() {
        ArrayList<Temp> res = new ArrayList<Temp>();
        res.add(this.dst);
        return res;
    }
    
}
