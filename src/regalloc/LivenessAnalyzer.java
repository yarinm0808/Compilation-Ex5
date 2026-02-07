package regalloc;

import ir.*;
import temp.Temp;
import java.util.*;

public class LivenessAnalyzer {
    
    private List<IrCommand> commands;
    private Map<String, Integer> labelMap = new HashMap<>();
    
    // Storage for the sets at each instruction index
    private Map<Integer, Set<Temp>> liveIn = new HashMap<>();
    private Map<Integer, Set<Temp>> liveOut = new HashMap<>();

    public LivenessAnalyzer(IrCommandList commandList) {
        this.commands = flattenList(commandList);
        buildLabelMap();
    }

    /**
     * Converts the recursive IrCommandList into a flat ArrayList 
     * for easy index-based access and successor tracking.
     */
    private List<IrCommand> flattenList(IrCommandList list) {
        List<IrCommand> flat = new ArrayList<>();
        IrCommandList curr = list;
        while (curr != null && curr.head != null) {
            flat.add(curr.head);
            curr = curr.tail;
        }
        return flat;
    }

    /**
     * Scans the IR to find all labels and store their instruction indices.
     */
    private void buildLabelMap() {
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i) instanceof IrCommandLabel) {
                // Accesses the labelName field from your IrCommandLabel class
                IrCommandLabel l = (IrCommandLabel) commands.get(i);
                labelMap.put(l.labelName, i);
            }
        }
    }

    /**
     * Performs the iterative liveness analysis until the sets converge.
     * Then builds and returns the Interference Graph.
     */
    public graph buildInterferenceGraph() {
        int n = commands.size();
        
        // Initialize sets for every instruction
        for (int i = 0; i < n; i++) {
            liveIn.put(i, new HashSet<>());
            liveOut.put(i, new HashSet<>());
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            
            // Process instructions in reverse order for efficient propagation
            for (int i = n - 1; i >= 0; i--) {
                IrCommand cmd = commands.get(i);
                
                Set<Temp> oldIn = new HashSet<>(liveIn.get(i));
                Set<Temp> oldOut = new HashSet<>(liveOut.get(i));

                // --- STEP 1: CALCULATE LIVEOUT[i] ---
                Set<Temp> newOut = new HashSet<>();
                
                if (cmd instanceof IrCommandJumpLabel) {
                    // Unconditional Jump: only successor is the target label
                    String target = ((IrCommandJumpLabel) cmd).labelName;
                    if (labelMap.containsKey(target)) {
                        newOut.addAll(liveIn.get(labelMap.get(target)));
                    }
                } 
                else if (cmd instanceof IrCommandJumpIfEqToZero) {
                    // Conditional Jump: two successors (target label AND next instruction)
                    String target = ((IrCommandJumpIfEqToZero) cmd).labelName;
                    if (labelMap.containsKey(target)) {
                        newOut.addAll(liveIn.get(labelMap.get(target)));
                    }
                    if (i < n - 1) {
                        newOut.addAll(liveIn.get(i + 1));
                    }
                } 
                else if (i < n - 1) {
                    // Standard instruction: successor is the next instruction
                    newOut.addAll(liveIn.get(i + 1));
                }
                
                liveOut.put(i, newOut);

                // --- STEP 2: CALCULATE LIVEIN[i] ---
                // Equation: In = Use ∪ (Out - Def)
                Set<Temp> newIn = new HashSet<>(newOut);
                
                List<Temp> defs = cmd.GetDefTemps();
                if (defs != null) {
                    newIn.removeAll(defs);
                }
                
                List<Temp> uses = cmd.GetUsedTemps();
                if (uses != null) {
                    newIn.addAll(uses);
                }
                
                liveIn.put(i, newIn);

                // Check for convergence
                if (!newIn.equals(oldIn) || !newOut.equals(oldOut)) {
                    changed = true;
                }
            }
        }

        

        // --- STEP 3: CONSTRUCT THE INTERFERENCE GRAPH ---
        graph g = new graph();
        for (int i = 0; i < n; i++) {
            IrCommand cmd = commands.get(i);
            List<Temp> defs = cmd.GetDefTemps();
            Set<Temp> out = liveOut.get(i);
            
            if (defs != null) {
                for (Temp d : defs) {
                    g.addVertex(d); // Ensure defined temp is in the graph
                    for (Temp l : out) {
                        g.addVertex(l); // Ensure live temp is in the graph
                        if (!d.equals(l)) {
                            g.addEdge(d, l);
                        }
                    }
                }
            }
            
            // Also ensure temps that are only used (not defined) are in the graph
            List<Temp> uses = cmd.GetUsedTemps();
            if (uses != null) {
                for (Temp u : uses) g.addVertex(u);
            }
        }
        
        return g;
    }
}