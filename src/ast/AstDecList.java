package ast;

import types.*;
import temp.*;
import ir.*;
import mips.MipsGenerator;
import java.util.ArrayList;
import java.util.List;

public class AstDecList extends AstNode {
    public AstDec head;
    public AstDecList tail;

    // A static list to collect all classes found during semantic analysis
    // across the entire linked list of declarations.
    private static List<TypeClass> allClassTypes = new ArrayList<>();

    public AstDecList(AstDec head, AstDecList tail) {
        serialNumber = AstNodeSerialNumber.getFresh();
        this.head = head;
        this.tail = tail;
    }

    public void printMe() {
        System.out.print("AST NODE DEC LIST\n");
        if (head != null) head.printMe();
        if (tail != null) tail.printMe();

        AstGraphviz.getInstance().logNode(serialNumber, "DEC\nLIST\n");
        if (head != null) AstGraphviz.getInstance().logEdge(serialNumber, head.serialNumber);
        if (tail != null) AstGraphviz.getInstance().logEdge(serialNumber, tail.serialNumber);
    }

    public Type semantMe() {


        if (head != null) {
            Type t = head.semantMe();
            // If the declaration was a class (e.g., 'class Father ...'), save it for VMT generation
            if (t instanceof TypeClass) {
                allClassTypes.add((TypeClass) t);
            }
        }

        if (tail != null) {
            tail.semantMe();
        }

        return null;
    }

    public Temp irMe() {
        // Only the top-most list node should trigger VMT generation
        // We do this by checking if there's any work to do and doing it once
        if (!allClassTypes.isEmpty()) {
            for (TypeClass tc : allClassTypes) {
                generateVmtForClass(tc);
            }
            // Clear it so recursive calls to tail.irMe() don't print duplicates
            allClassTypes.clear(); 
        }

        if (head != null) head.irMe();
        if (tail != null) tail.irMe();

        return null;
    }

    /**
     * Helper to build the VMT for a specific class and send it to MIPS
     */
    private void generateVmtForClass(TypeClass tc) {
        List<String> methodLabels = new ArrayList<>();
        
        // 1. Get the fixed order of methods [SWIM, RUN, WALK]
        List<String> methodNames = tc.getVMTOrder(); 
        
        for (String mName : methodNames) {
            // 2. Find which class level actually defines this method for 'tc'
            Type member = tc.findFieldType(mName);
            if (member instanceof TypeFunction) {
                TypeFunction tf = (TypeFunction) member;
                // 3. Create the label: DefiningClass_MethodName (e.g., Father_SWIM)
                // This requires that you added 'definingClassName' to TypeFunction!
                methodLabels.add(tf.definingClassName + "_" + mName);
            }
        }

        // 4. Output to the .data section of the assembly file
        MipsGenerator.getInstance().printVMT(tc.name, methodLabels);
    }
}