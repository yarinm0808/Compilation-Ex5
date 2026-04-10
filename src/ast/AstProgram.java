package ast;

import java.util.ArrayList;
import java.util.List;
import types.*;
import ir.*;
import mips.MipsGenerator;
import temp.Temp;

public class AstProgram extends AstNode {
    public List<AstDec> decList;
    // List to keep track of all classes defined in the program
    private List<TypeClass> classTypes = new ArrayList<>();

    public AstProgram(AstDec firstDec) {
        this.decList = new ArrayList<>();
        if (firstDec != null) this.decList.add(firstDec);
    }

    public void add(AstDec dec) {
        if (dec != null) this.decList.add(dec);
    }

    public Type semantMe() {
        for (AstDec dec : decList) {
            Type t = dec.semantMe();
            // If the declaration was a class, save its type for VMT generation later
            if (t instanceof TypeClass) {
                classTypes.add((TypeClass) t);
            }
        }
        return null;
    }

    public Temp irMe() {
        // 1. Generate VMTs in the .data section first
        for (TypeClass tc : classTypes) {
            generateVmtForClass(tc);
        }

        // 2. Generate IR for all declarations (functions, globals, etc.)
        for (AstDec dec : decList) {
            dec.irMe();
        }
        return null;
    }

    private void generateVmtForClass(TypeClass tc) {
        // This helper should find the most specific label for each method slot
        List<String> methodLabels = new ArrayList<>();
        
        // Use the fixed order defined in TypeClass
        List<String> methodNames = tc.getVMTOrder(); 
        
        for (String mName : methodNames) {
            // Find the class that actually implements this method for tc
            TypeFunction tf = (TypeFunction) tc.findFieldType(mName);
            // Label format: DefiningClassName_MethodName (e.g., Father_SWIM)
            methodLabels.add(tf.definingClassName + "_" + mName);
        }

        // Tell the MipsGenerator to print the .word table
        MipsGenerator.getInstance().printVMT(tc.name, methodLabels);
    }
}