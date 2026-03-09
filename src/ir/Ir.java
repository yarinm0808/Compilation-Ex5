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
        int i = 0;
        while (i < allCommands.size()) {
            IrCommand current = allCommands.get(i);

            if (current instanceof IrCommandLabel || current instanceof IrCommandPrologue) {
                // --- EXISTING FUNCTION BLOCK LOGIC ---
                int start = i;
                int end = i + 1;
                while (end < allCommands.size() && 
                    !(allCommands.get(end) instanceof IrCommandPrologue) &&
                    !(allCommands.get(end) instanceof IrCommandLabel)) {
                    end++;
                }
                List<IrCommand> funcBody = allCommands.subList(start, end);
                
                // Register allocation and MIPS emission for the function
                LivenessAnalyzer la = new LivenessAnalyzer(funcBody);
                Map<Temp, String> regMap = la.buildInterferenceGraph().graphColor10();
                for (IrCommand cmd : funcBody) {
                    cmd.mipsMe(regMap);
                }
                i = end; 
            } else {
                // --- NEW GLOBAL COMMAND LOGIC ---
                // If it's a global allocation, it doesn't need register mapping!
                System.out.println("[DEBUG] Processing Global Command: " + current.getClass().getSimpleName());
                current.mipsMe(null); 
                i++;
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

    protected Ir() {}

    public static Ir getInstance() {
        if (instance == null) {
            instance = new Ir();
        }
        return instance;
    }
}