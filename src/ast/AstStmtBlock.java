package ast;
import temp.*;

public class AstStmtBlock extends AstStmt {
    public AstStmtList body;
    public AstStmtBlock(AstStmtList body) {
        this.serialNumber = AstNodeSerialNumber.getFresh();
        this.body = body;
    }
    public void printMe() {
        if (body != null) body.printMe();
    }
    public Temp irMe() {
        if (body != null) return body.irMe();
        return null;
    }
}