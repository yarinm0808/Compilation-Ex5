package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mips.*;
import temp.Temp;

public class IrCommandPush extends IrCommand {
    public Temp ToPush;
    public IrCommandPush(Temp ToPush){
        this.ToPush = ToPush;
    }

    @Override
    public void mipsMe(Map<Temp, String> regMap) {
        String ArgToPush = regMap.get(this.ToPush);
        MipsGenerator.getInstance().push(ArgToPush);
    }

    @Override
    public List<Temp> GetUsedTemps() {
        List<Temp> temps = new ArrayList<Temp>();
        temps.add(this.ToPush);
        return temps;
    }

    @Override
    public List<Temp> GetDefTemps() {
        return new ArrayList<Temp>();
    }
    
}
