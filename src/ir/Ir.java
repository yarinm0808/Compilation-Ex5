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
        if (allCommands.isEmpty())
            return;

        List<IrCommand> globalAllocations = new ArrayList<>();
        List<IrCommand> globalInstructions = new ArrayList<>();
        List<Integer> functionStartIndices = new ArrayList<>();

        // 1. Find function boundaries
        for (int j = 0; j < allCommands.size(); j++) {
            if (allCommands.get(j) instanceof IrCommandPrologue) {
                if (j > 0 && allCommands.get(j - 1) instanceof IrCommandLabel) {
                    functionStartIndices.add(j - 1);
                } else {
                    functionStartIndices.add(j);
                }
            }
        }

        // 2. Separate Data from Logic
        int firstFuncIdx = functionStartIndices.isEmpty() ? allCommands.size() : functionStartIndices.get(0);
        Set<String> seenStrings = new java.util.HashSet<>();
        Set<String> seenVMTs = new java.util.HashSet<>();
        for (int j = 0; j < allCommands.size(); j++) {
            IrCommand cmd = allCommands.get(j);
            String name = cmd.getClass().getSimpleName();

            if (name.equals("IrCommandAllocateString")) {
                // HOIST: Grab strings from EVERYWHERE in the program
                String lbl = ((IrCommandAllocateString)cmd).name; 
        
                if (!seenStrings.contains(lbl)) {
                    seenStrings.add(lbl);
                    globalAllocations.add(cmd);
                }
            }
            // else if (name.equals("IrCommandAllocateVMT")) {
            //     // DEDUPLICATE VMTs!
            //     // (Change 'className' to whatever variable holds "counter" in your class)
            //     String vmtName = ((IrCommandAllocateVMT)cmd).className; 
                
            //     if (!seenVMTs.contains(vmtName)) {
            //         seenVMTs.add(vmtName);
            //         globalAllocations.add(cmd);
            //     }
            // } 
            else if (j < firstFuncIdx) {
                // GLOBALS: Grab variables/init logic only from BEFORE the first function
                if (name.equals("IrCommandAllocate")) {
                    globalAllocations.add(cmd);
                } else if (!(cmd instanceof IrCommandLabel)) {
                    globalInstructions.add(cmd);
                }
            }
        }

        MipsGenerator mips = MipsGenerator.getInstance();

        // 3. PHASE A: Consolidated .data section (No Tabs!)
        mips.addnotab(".data");
        mips.addnotab(".align 2");
        for (IrCommand cmd : globalAllocations) {
            // Ensure your Allocate commands use addnotab for the "var: .word 0" line
            cmd.mipsMe(null);
        }

        // 4. PHASE B: Global Init Block
        mips.addnotab(".text");
        mips.addnotab("_global_init:"); // Label flush left
        if (!globalInstructions.isEmpty()) {
            regalloc.LivenessAnalyzer laGlobal = new regalloc.LivenessAnalyzer(globalInstructions);
            Map<temp.Temp, String> globalRegMap = laGlobal.buildInterferenceGraph().graphColor10();
            for (IrCommand cmd : globalInstructions) {
                cmd.mipsMe(globalRegMap); // Instructions here will be indented normally
            }
        }
        mips.add("jr $ra"); // \tjr $ra

        // 5. PHASE C: Process each Function Bucket
        for (int k = 0; k < functionStartIndices.size(); k++) {
            int start = functionStartIndices.get(k);
            int end = (k + 1 < functionStartIndices.size()) ? functionStartIndices.get(k + 1) : allCommands.size();

            List<IrCommand> funcBody = new ArrayList<>(allCommands.subList(start, end));

            if (!funcBody.isEmpty()) {
                regalloc.LivenessAnalyzer la = new regalloc.LivenessAnalyzer(funcBody);
                Map<temp.Temp, String> regMap = la.buildInterferenceGraph().graphColor10();
                for (IrCommand cmd : funcBody) {
                    // NEW CHECK: Skip strings here, they are already at the top!
                    if (cmd.getClass().getSimpleName().equals("IrCommandAllocateString")) {
                        continue; 
                    }
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