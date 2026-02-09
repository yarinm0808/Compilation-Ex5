package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mips.*;
import temp.Temp;

public class IrCommandStackPointUpdate extends IrCommand{
    public int bytes;
    public IrCommandStackPointUpdate(int bytes){
        this.bytes = bytes;
    }
    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        MipsGenerator.getInstance().StackPointerUpdate(this.bytes); 
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
