package ast;

import java.util.ArrayList;
import java.util.List;

public class AstProgram extends AstNode {
    public List<AstDec> decList;

    public AstProgram(AstDec firstDec) {
        this.decList = new ArrayList<>();
        this.decList.add(firstDec);
    }

    public void add(AstDec dec) {
        this.decList.add(dec);
    }
}