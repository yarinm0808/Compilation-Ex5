package ast;

import ir.*;
import temp.Temp;
import temp.TempFactory;
import types.*;


public class AstExpNil extends AstExp
{
    public AstExpNil()
    {
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.print("AST NODE NIL\n");
    }

    public void printMe()
    {
        System.out.print("AST NODE NIL\n");
        AstGraphviz.getInstance().logNode(serialNumber, "NIL");
    }

    public Type semantMe()
    {
        // You typically return a special TypeNil.getInstance() or TypeVoid.
        return TypeVoid.getInstance(); 
    }

    public Temp irMe() {
        Temp dst = TempFactory.getInstance().getFreshTemp();
        // Add an IR command that loads the constant 0 into the destination temporary
        Ir.getInstance().AddIrCommand(new IRcommandConstInt(dst, 0));
        return dst;
    }

}