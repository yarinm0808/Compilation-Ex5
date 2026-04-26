package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        if (cmd == null) return;

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
        List<IrCommand> globalCommands = new ArrayList<>();
        
        int i = 0;
        
        // ---------------------------------------------------------
        // [PHASE 1] Extract Global Commands
        // Collect everything before the first function starts.
        // ---------------------------------------------------------
        while (i < allCommands.size()) {
            IrCommand current = allCommands.get(i);
            
            // Stop collecting globals when we hit the first function boundary
            if (current instanceof IrCommandLabel || current instanceof IrCommandPrologue) {
                break; 
            }
            
            // --- THE FIX ---
            // If it's just a memory declaration (.word 0), process it immediately!
            // (Make sure IrCommandAllocate is imported, or use the string comparison below)
            if (current.getClass().getSimpleName().equals("IrCommandAllocate")) {
                current.mipsMe(null);
            } else {
                // It's an initialization instruction (math, loads, stores). Inject into main!
                globalCommands.add(current);
            }
            i++;
        }

        // ---------------------------------------------------------
        // [PHASE 2] Chunk and Process Functions
        // ---------------------------------------------------------
        while (i < allCommands.size()) {
            IrCommand current = allCommands.get(i);

            int start = i;
            int end = i + 1;
            
            // Advance 'end' until the next function boundary
            while (end < allCommands.size() && 
                   !(allCommands.get(end) instanceof IrCommandPrologue) &&
                   !(allCommands.get(end) instanceof IrCommandLabel)) {
                end++;
            }
            
            // Create a mutable copy of the function body
            List<IrCommand> funcBody = new ArrayList<>(allCommands.subList(start, end));

            // --- THE INJECTION TRAP ---
            boolean isMain = false;
            for (IrCommand cmd : funcBody) {
                // Check if it's your label command
                if (cmd instanceof IrCommandLabel) {
                    IrCommandLabel lbl = (IrCommandLabel) cmd;
                    // IMPORTANT: Change '.name' to whatever field holds your label string!
                    if (lbl.labelName != null && lbl.labelName.contains("main")) {
                        isMain = true;
                        break;
                    }
                }
            }

            // If it's main, inject the globals right after the prologue/label!
            if (isMain && !globalCommands.isEmpty()) {
                // Insert at index 1 (right after the "func_main:" label)
                funcBody.addAll(1, globalCommands);
                globalCommands.clear(); 
            }

            // Register allocation and MIPS emission for the function
            LivenessAnalyzer la = new LivenessAnalyzer(funcBody);
            Map<Temp, String> regMap = la.buildInterferenceGraph().graphColor10();
            
            for (IrCommand cmd : funcBody) {
                // Now, if this is 'main', the global commands are safely 
                // inside 'funcBody' and will receive a valid regMap!
                cmd.mipsMe(regMap);
            }
            
            i = end; 
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

    protected Ir() {}

    public static Ir getInstance() {
        if (instance == null) {
            instance = new Ir();
        }
        return instance;
    }
}