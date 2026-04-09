package ir;

import java.util.*;
import ast.AstNodeSerialNumber;
import temp.*;
import mips.*;

public class IrCommandBinopConcatStrings extends IrCommand {
    public Temp dst;
    public Temp left;
    public Temp right;

    public IrCommandBinopConcatStrings(Temp dst, Temp left, Temp right) {
        this.dst = dst;
        this.left = left;
        this.right = right;
    }

    @Override
    public List<Temp> GetUsedTemps() {
        return Arrays.asList(left, right);
    }

    @Override
    public List<Temp> GetDefTemps() {
        return Arrays.asList(dst);
    }

    @Override
    public void mipsMe(Map<Temp, String> tempToReg) {
        String leftReg  = tempToReg.get(left);
        String rightReg = tempToReg.get(right);
        String dstReg   = tempToReg.get(dst);

        MipsGenerator gen = MipsGenerator.getInstance();
        String suffix = "_" + AstNodeSerialNumber.getFresh();

        // -----------------------------------------------------------------
        // [STEP 0] PROTECT SOURCE REGISTERS
        // Capture originals into $s registers[cite: 101]. This prevents
        // bugs if the allocator gave dstReg the same register as leftReg or rightReg.
        // -----------------------------------------------------------------
        gen.Move("$s3", leftReg);
        gen.Move("$s4", rightReg);

        // -----------------------------------------------------------------
        // [STEP 1] CALCULATE TOTAL LENGTH
        // -----------------------------------------------------------------
        gen.li("$a0", 0);
        gen.Move("$s0", "$s3"); // Cursor for string 1

        gen.label("label_len1" + suffix);
        gen.load_byte("$s1", "$s0", 0);
        gen.beqz("$s1", "label_len2" + suffix);
        
        // Length counter ($a0) is an L integer - results must be saturated[cite: 18, 55].
        gen.addi("$a0", "$a0", 1); 
        
        // POINTER INCREMENT ($s0): RAW (Addresses must not be saturated)[cite: 100].
        gen.addiu("$s0", "$s0", 1); 
        gen.jump("label_len1" + suffix);

        gen.label("label_len2" + suffix);
        gen.Move("$s0", "$s4"); // Cursor for string 2
        gen.label("label_loop_len2" + suffix);
        gen.load_byte("$s1", "$s0", 0);
        gen.beqz("$s1", "label_alloc" + suffix);
        gen.addi("$a0", "$a0", 1); // Saturated
        gen.addiu("$s0", "$s0", 1); // RAW
        gen.jump("label_loop_len2" + suffix);

        // -----------------------------------------------------------------
        // [STEP 2] ALLOCATE HEAP MEMORY
        // -----------------------------------------------------------------
        gen.label("label_alloc" + suffix);
        gen.addi("$a0", "$a0", 1); // +1 for null terminator[cite: 22].
        gen.li("$v0", 9);          // malloc (sbrk) system call[cite: 84, 87].
        gen.add("syscall");
        
        // Destination register is now safe to update as sources are in $s3/$s4.
        gen.Move(dstReg, "$v0"); 

        // -----------------------------------------------------------------
        // [STEP 3] COPY STRINGS
        // -----------------------------------------------------------------
        gen.Move("$s0", "$s3");    // Reset source cursor to start of String 1
        gen.Move("$s2", dstReg);   // Destination cursor starts at new heap address
        
        gen.label("label_copy1" + suffix);
        gen.load_byte("$s1", "$s0", 0);
        gen.beqz("$s1", "label_copy2" + suffix);
        gen.store_byte("$s1", "$s2", 0);
        gen.addiu("$s0", "$s0", 1); // RAW increment
        gen.addiu("$s2", "$s2", 1); // RAW increment
        gen.jump("label_copy1" + suffix);

        gen.label("label_copy2" + suffix);
        gen.Move("$s0", "$s4");    // Source cursor to String 2
        
        gen.label("label_loop_copy2" + suffix);
        gen.load_byte("$s1", "$s0", 0);
        gen.beqz("$s1", "label_done" + suffix);
        gen.store_byte("$s1", "$s2", 0);
        gen.addiu("$s0", "$s0", 1); // RAW increment
        gen.addiu("$s2", "$s2", 1); // RAW increment
        gen.jump("label_loop_copy2" + suffix);

        // -----------------------------------------------------------------
        // [STEP 4] NULL TERMINATION
        // -----------------------------------------------------------------
        gen.label("label_done" + suffix);
        gen.store_byte("$zero", "$s2", 0); // Resulting string must be null terminated.
    }
}