package ast;

public class AstArg extends AstNode {
    public AstType type;
    public String name;

    public AstArg(AstType type, String name) {
        this.type = type;
        this.name = name;
    }
}