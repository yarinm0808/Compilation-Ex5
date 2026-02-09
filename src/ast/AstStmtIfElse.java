package ast;
import temp.*;
import ir.*;
import types.*;

public class AstStmtIfElse extends AstStmt{
    public AstExp cond;
    public AstStmtList body;
    public AstStmtList elze;

    public AstStmtIfElse(AstExp cond, AstStmtList body, AstStmtList elze){
        this.cond = cond;
        this.body = body;
        this.elze = elze;
    }
    
    @Override
    public Type semantMe() {
        Type t = cond.semantMe();
        if (!(t instanceof TypeInt)) {
            throw new RuntimeException("ERROR(" + lineNumber + ")");
        }
        if (body != null) body.semantMe();
        if (elze != null) elze.semantMe();
        return null;
    }
    
    @Override
    public Temp irMe() {
        /**********************************************************************/
        /* [1] Create unique labels using your existing IrCommand method      */
        /**********************************************************************/
        String labelElseStr = IrCommand.getFreshLabel("Else");
        String labelEndStr  = IrCommand.getFreshLabel("IfEnd");

        /**********************************************************************/
        /* [2] Evaluate the condition expression                              */
        /**********************************************************************/
        Temp condTemp = cond.irMe();

        /**********************************************************************/
        /* [3] If condition is FALSE (0), jump to the Else block              */
        /**********************************************************************/
        Ir.getInstance().AddIrCommand(new IrCommandJumpIfEqToZero(condTemp, labelElseStr));

        /**********************************************************************/
        /* [4] Generate the "Then" body (if it exists)                        */
        /**********************************************************************/
        if (body != null) {
            body.irMe();
        }

        /**********************************************************************/
        /* [5] Jump to the end to skip the Else block                         */
        /**********************************************************************/
        Ir.getInstance().AddIrCommand(new IrCommandJump(labelEndStr));

        /**********************************************************************/
        /* [6] Mark the start of the Else block                               */
        /**********************************************************************/
        Ir.getInstance().AddIrCommand(new IrCommandLabel(labelElseStr));

        /**********************************************************************/
        /* [7] Generate the "Else" body (if it exists)                        */
        /**********************************************************************/
        if (elze != null) {
            elze.irMe();
        }

        /**********************************************************************/
        /* [8] Mark the end of the entire structure                           */
        /**********************************************************************/
        Ir.getInstance().AddIrCommand(new IrCommandLabel(labelEndStr));

        return null;
    }
    
}
