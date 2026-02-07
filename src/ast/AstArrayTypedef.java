package ast;

public class AstArrayTypedef extends AstNode{
    public String name;
    public AstType type;

    public AstArrayTypedef(String name, AstType type){
        this.name = name;
        this.type = type;
    }

}
