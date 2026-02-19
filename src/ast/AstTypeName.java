package ast;

import types.*;
import symboltable.*;

public class AstTypeName extends AstNode
{
    public String type;
    public String name;
    
    /***************************************************************/
    /* NEW: Store the entry so irMe can find the stack offset later */
    /***************************************************************/
    public SymbolTableEntry entry; 

    public AstTypeName(String type, String name)
    {
        serialNumber = AstNodeSerialNumber.getFresh();
        this.type = type;
        this.name = name;
    }

    public void printMe()
    {
        System.out.format("NAME(%s):TYPE(%s)\n",name,type);
        AstGraphviz.getInstance().logNode(
                serialNumber,
            String.format("NAME:TYPE\n%s:%s",name,type));
    }

    public Type semantMe()
    {
        Type t = SymbolTable.getInstance().find(type);
        if (t == null)
        {
            System.out.format(">> ERROR: non existing type %s\n", type);
            System.exit(0);
            return null;
        }
        else
        {
            /***********************************************************/
            /* NEW: Capture the entry returned by enter() and store it */
            /***********************************************************/
            this.entry = SymbolTable.getInstance().enter(name, t);
        }

        return t;
    }   
}