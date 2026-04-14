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
        if (t == null) return;
        if (!edges.containsKey(t)) {
            this.vertices.add(t);
            this.edges.put(t, new ArrayList<>());
        }
    }

    public void addEdge(Temp a, Temp b) {
        if (a == null || b == null || a.equals(b)) return;
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
        // System.out.println(">> [DEBUG] Starting Register Allocation for " + vertices.size() + " variables.");
        
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
            
            for (Temp t : nodesToProcess) {
                if (currentDegrees.get(t) < 10) {
                    target = t;
                    break;
                }
            }

            if (target == null) {
                // --- ENHANCED ERROR REPORTING ---
                // System.err.println("\n!!! [CRITICAL] Register Allocation Failed !!!");
                // System.err.println("The following variables have 10 or more interferences and cannot be simplified:");
                // for (Temp t : nodesToProcess) {
                //     System.err.format(" - %s (Current Degree: %d)\n", t, currentDegrees.get(t));
                //     System.err.print("     Interferes with: ");
                //     for (Temp neighbor : edges.get(t)) {
                //         if (!removedNodes.contains(neighbor)) System.err.print(neighbor + " ");
                //     }
                //     System.err.println();
                // }
                // System.err.println("TIP: Check your AstStmtAssign. Evaluate the Right-Hand Side before the Left-Hand Side.\n");
                throw new RuntimeException("Register Allocation Failed");
            }

            // Log the simplification
            // System.out.println(">> [DEBUG] Simplifying node: " + target + " (Degree: " + currentDegrees.get(target) + ")");
            
            nodesToProcess.remove(target);
            removedNodes.add(target);
            selectStack.push(target);

            for (Temp neighbor : edges.get(target)) {
                if (!removedNodes.contains(neighbor)) {
                    currentDegrees.put(neighbor, currentDegrees.get(neighbor) - 1);
                }
            }
        }

        // PHASE 2: SELECT (COLORING)
        Map<Temp, String> coloring = new HashMap<>();
        String[] physicalRegisters = {"$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7", "$t8", "$t9"};

        // System.out.println(">> [DEBUG] Phase 1 Complete. Starting Select Phase.");

        while (!selectStack.isEmpty()) {
            Temp t = selectStack.pop();
            
            Set<String> forbiddenColors = new HashSet<>();
            for (Temp neighbor : edges.get(t)) {
                if (coloring.containsKey(neighbor)) {
                    forbiddenColors.add(coloring.get(neighbor));
                }
            }

            String assignedReg = null;
            for (String reg : physicalRegisters) {
                if (!forbiddenColors.contains(reg)) {
                    assignedReg = reg;
                    break;
                }
            }
            
            // System.out.println(">> [DEBUG] Color assigned: " + t + " -> " + assignedReg);
            coloring.put(t, assignedReg);
        }

        return coloring;
    }
}