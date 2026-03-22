package types;

public class TypeClassVarDec extends Type
{
	public Type t;
	public String name;
	
	public TypeClassVarDec(Type t, String name)
	{
		this.t = t;
		this.name = name;
	}
}
