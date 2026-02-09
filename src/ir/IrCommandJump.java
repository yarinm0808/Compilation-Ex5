package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import temp.Temp;
import mips.*;
public class IrCommandJump extends IrCommand {
    public String target_label;
    public IrCommandJump(String label){
        this.target_label = label;
    }


    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        MipsGenerator.getInstance().jump(this.target_label);
    }

    @Override
    public List<Temp> GetUsedTemps() {
        return new ArrayList<Temp>();
    }

    @Override
    public List<Temp> GetDefTemps() {
        return new ArrayList<Temp>();   
    }
    
}
