package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mips.MipsGenerator;
import regalloc.LivenessAnalyzer;
import regalloc.graph;
import temp.Temp;

public class Ir {
    // These should ideally match the structure of IrCommandList
    // to keep the iteration consistent.
    public IrCommand head = null;
    public IrCommandList tail = null;

    /**
     * Appends a new command to the end of the global IR list.
     */
    public void AddIrCommand(IrCommand cmd) {
        if (cmd == null)
            return;

        if (head == null) {
            this.head = cmd;
        } else if (tail == null) {
            this.tail = new IrCommandList(cmd, null);
        } else {
            IrCommandList it = tail;
            while (it.tail != null) {
                it = it.tail;
            }
            it.tail = new IrCommandList(cmd, null);
        }
    }

    public List<IrCommand> flatten() {
        List<IrCommand> list = new ArrayList<>();
        if (this.head != null) {
            list.add(this.head);
        }
        IrCommandList curr = this.tail;
        while (curr != null && curr.head != null) {
            list.add(curr.head);
            curr = curr.tail;
        }
        return list;
    }

    /**
     * Triggers MIPS generation for the entire program using the
     * finalized register mapping.
     */
    public void mipsMe() {
        List<IrCommand> allCommands = flatten();
        if (allCommands.isEmpty()) return;

        List<IrCommand> globalAllocations = new ArrayList<>();
        List<IrCommand> globalInstructions = new ArrayList<>();
        List<List<IrCommand>> allFunctions = new ArrayList<>();
        List<IrCommand> currentFunction = null;
        boolean inFunction = false;

        Set<String> seenStrings = new java.util.HashSet<>();

        // 1 & 2. Smart Categorization
        // 1 & 2. Smart Categorization
        for (int j = 0; j < allCommands.size(); j++) {
            IrCommand cmd = allCommands.get(j);
            String name = cmd.getClass().getSimpleName();

            // Handle Strings
            if (name.equals("IrCommandAllocateString")) {
                String lbl = ((IrCommandAllocateString)cmd).name; 
                if (!seenStrings.contains(lbl)) {
                    seenStrings.add(lbl);
                    globalAllocations.add(cmd);
                }
                continue;
            }

            // Handle Global Memory Allocation
            if (name.equals("IrCommandAllocate")) {
                globalAllocations.add(cmd);
                continue;
            }

            // Detect Function START
            if (name.equals("IrCommandPrologue")) {
                inFunction = true;
                currentFunction = new ArrayList<>();
                // Grab the label if it was right before this prologue
                if (j > 0 && allCommands.get(j - 1) instanceof IrCommandLabel) {
                    currentFunction.add(allCommands.get(j - 1));
                    // Remove it from globalInstructions where it just landed
                    if (!globalInstructions.isEmpty() && globalInstructions.get(globalInstructions.size() - 1) == allCommands.get(j - 1)) {
                        globalInstructions.remove(globalInstructions.size() - 1);
                    }
                }
                currentFunction.add(cmd);
                allFunctions.add(currentFunction);
                continue;
            }

            // Route the instruction!
            if (inFunction) {
                currentFunction.add(cmd);
                
                // CRITICAL FIX: Detect Function END at JumpToRa, not Epilogue!
                if (name.equals("IrCommandJumpToRa")) {
                    inFunction = false; // Next commands will be global!
                }
            } else {
                // If it's a label right before a function, ignore it (handled above)
                if (cmd instanceof IrCommandLabel && j + 1 < allCommands.size() && allCommands.get(j + 1) instanceof IrCommandPrologue) {
                    continue;
                }
                
                // CRITICAL FIX: Strip any stray jr $ra commands from the global scope!
                if (name.equals("IrCommandJumpToRa")) {
                    continue;
                }
                
                globalInstructions.add(cmd);
            }
        }

        MipsGenerator mips = MipsGenerator.getInstance();

        // 3. PHASE A: Consolidated .data section (No Tabs!)
        mips.addnotab(".data");
        mips.addnotab(".align 2");
        for (IrCommand cmd : globalAllocations) {
            cmd.mipsMe(null);
        }

        // 4. PHASE B: Global Init Block
        mips.addnotab(".text");
        mips.addnotab("_global_init:"); 
        
        // --- Prologue for _global_init ---
        mips.add("subu $sp, $sp, 4");
        mips.add("sw $ra, 0($sp)");

        if (!globalInstructions.isEmpty()) {
            regalloc.LivenessAnalyzer laGlobal = new regalloc.LivenessAnalyzer(globalInstructions);
            Map<temp.Temp, String> globalRegMap = laGlobal.buildInterferenceGraph().graphColor10();
            for (IrCommand cmd : globalInstructions) {
                cmd.mipsMe(globalRegMap); 
            }
        }

        // --- Epilogue for _global_init ---
        mips.add("lw $ra, 0($sp)");
        mips.add("addu $sp, $sp, 4");
        mips.add("jr $ra");

        // 5. PHASE C: Process each Function Bucket
        for (List<IrCommand> funcBody : allFunctions) {
            if (!funcBody.isEmpty()) {
                regalloc.LivenessAnalyzer la = new regalloc.LivenessAnalyzer(funcBody);
                Map<temp.Temp, String> regMap = la.buildInterferenceGraph().graphColor10();
                
                for (IrCommand cmd : funcBody) {
                    cmd.mipsMe(regMap);
                }
            }
        }
    }

    /**
     * Helper to get the full list for the LivenessAnalyzer.
     * This creates a wrapper list starting from the head.
     */
    public IrCommandList getFullList() {
        return new IrCommandList(head, tail);
    }

    /**************************************/
    /* USUAL SINGLETON IMPLEMENTATION ... */
    /**************************************/
    private static Ir instance = null;

    protected Ir() {
    }

    public static Ir getInstance() {
        if (instance == null) {
            instance = new Ir();
        }
        return instance;
    }
}