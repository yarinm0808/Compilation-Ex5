package regalloc;

import java.util.*;
import temp.*;

public class graph {
    public List<Temp> vertices;
    public Map<Temp, List<Temp>> edges;
    
    public graph() {
        this.vertices = new ArrayList<>();
        this.edges = new HashMap<>();
    }

    public void addVertex(Temp t) {
        if (!edges.containsKey(t)) {
            this.vertices.add(t);
            this.edges.put(t, new ArrayList<>());
        }
    }

    public void addEdge(Temp a, Temp b) {
        if (a.equals(b)) return;
        addVertex(a);
        addVertex(b);
        
        if (!edges.get(a).contains(b)) {
            this.edges.get(a).add(b);
            this.edges.get(b).add(a);
        }
    }

    /**
     * Implementation of the Simplification-based Register Allocation.
     * Returns a mapping of Temp -> Physical Register.
     */
    public Map<Temp, String> graphColor10() {
        // 1. Setup metadata for non-destructive simplification
        Stack<Temp> selectStack = new Stack<>();
        Map<Temp, Integer> currentDegrees = new HashMap<>();
        Set<Temp> removedNodes = new HashSet<>();
        
        for (Temp t : vertices) {
            currentDegrees.put(t, edges.get(t).size());
        }

        List<Temp> nodesToProcess = new ArrayList<>(vertices);

        // PHASE 1: SIMPLIFY
        while (!nodesToProcess.isEmpty()) {
            Temp target = null;
            
            // Find a node with degree < 10
            for (Temp t : nodesToProcess) {
                if (currentDegrees.get(t) < 10) {
                    target = t;
                    break;
                }
            }

            if (target == null) {
                // Requirement: Exit and print specific error if 10-coloring is impossible
                System.out.println("Register Allocation Failed");
                throw new RuntimeException("Register Allocation Failed");
            }

            // "Logically" remove the node
            nodesToProcess.remove(target);
            removedNodes.add(target);
            selectStack.push(target);

            // Update neighbors' degrees
            for (Temp neighbor : edges.get(target)) {
                if (!removedNodes.contains(neighbor)) {
                    currentDegrees.put(neighbor, currentDegrees.get(neighbor) - 1);
                }
            }
        }

        // PHASE 2: SELECT (COLORING)
        Map<Temp, String> coloring = new HashMap<>();
        String[] physicalRegisters = {"$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7", "$t8", "$t9"};

        

        while (!selectStack.isEmpty()) {
            Temp t = selectStack.pop();
            
            // Determine which registers are used by neighbors
            Set<String> forbiddenColors = new HashSet<>();
            for (Temp neighbor : edges.get(t)) {
                if (coloring.containsKey(neighbor)) {
                    forbiddenColors.add(coloring.get(neighbor));
                }
            }

            // Assign the first available register from $t0-$t9
            String assignedReg = null;
            for (String reg : physicalRegisters) {
                if (!forbiddenColors.contains(reg)) {
                    assignedReg = reg;
                    break;
                }
            }
            
            coloring.put(t, assignedReg);
        }

        return coloring;
    }
}