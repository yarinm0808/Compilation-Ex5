package ir;

import java.util.Map;
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

    /**
     * Triggers MIPS generation for the entire program using the 
     * finalized register mapping.
     */
    public void mipsMe(Map<Temp, String> regMap) {
        if (head != null) {
            head.mipsMe(regMap);
        }
        if (tail != null) {
            tail.mipsMe(regMap);
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