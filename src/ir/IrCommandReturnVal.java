package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import temp.Temp;
import mips.*;

public class IrCommandReturnVal extends IrCommand {
    public Temp valTemp;
    public IrCommandReturnVal(Temp valTemp){
        this.valTemp = valTemp;
    }

    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        String ArgReturnval = regMap.get(this.valTemp);
        MipsGenerator.getInstance().ReturnValue(ArgReturnval);
    }

    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<Temp>();
        temps.add(this.valTemp);
        return temps;
    }

    @Override
    public List<Temp> GetDefTemps() {
        return new ArrayList<Temp>();
    }
    
}
