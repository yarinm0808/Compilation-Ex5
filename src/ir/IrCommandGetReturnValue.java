package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import temp.Temp;
import mips.*;

public class IrCommandGetReturnValue extends IrCommand {
    public Temp res;
    public IrCommandGetReturnValue(Temp res){
        this.res = res;
    }
    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        String ArgRes = regMap.get(this.res);
        MipsGenerator.getInstance().GetRetVal(ArgRes);
    }

    @Override
    public List<Temp> GetUsedTemps() {
        return new ArrayList<Temp>();
    }

    @Override
    public List<Temp> GetDefTemps() {
        List<Temp> temps = new ArrayList<Temp>();
        temps.add(this.res);
        return temps;
    }
    
}
