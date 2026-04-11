package ir;

import temp.*;
import java.util.*;
import mips.*;
import ast.AstNodeSerialNumber;

public class IrCommandBoundsCheck extends IrCommand{

    public Temp ArrayRegTemp;
    public Temp IndexTemp;
    public IrCommandBoundsCheck(Temp ArrayRegTemp, Temp IndexTemp){
        this.ArrayRegTemp = ArrayRegTemp;
        this.IndexTemp = IndexTemp;
    }

	@Override
	public List<Temp> GetUsedTemps() {
		List<Temp> temps = new ArrayList<Temp>();
		if (this.ArrayRegTemp != null) temps.add(this.ArrayRegTemp);
        if (this.IndexTemp != null) temps.add(this.IndexTemp);
		return temps;
	}

	@Override
	public List<Temp> GetDefTemps() {
		List<Temp> temps = new ArrayList<Temp>();
        return temps;
	}

    public void mipsMe(Map<Temp, String> regMap) {
        String arrayReg = regMap.get(this.ArrayRegTemp);
        String indexReg = regMap.get(this.IndexTemp);
        MipsGenerator gen = MipsGenerator.getInstance();
        String errorLabel = "label_access_violation_" + AstNodeSerialNumber.getFresh();
        String okLabel = "label_bounds_ok_" + AstNodeSerialNumber.getFresh();

        // lw $at, 0(arrayBase)
        gen.add(String.format("\tlw $at, 0(%s)\n", arrayReg));
        
        // Bounds checks
        gen.add(String.format("\tbltz %s, %s\n", indexReg, errorLabel));
        gen.add(String.format("\tbge %s, $at, %s\n", indexReg, errorLabel));
        gen.add(String.format("\tj %s\n", okLabel));

        // Error block
        gen.add(errorLabel + ":\n");
        gen.add("\tla $a0, string_access_violation\n");
        gen.add("\tli $v0, 4\n\tsyscall\n\tli $v0, 10\n\tsyscall\n");

        gen.add(okLabel + ":\n");
    }
}