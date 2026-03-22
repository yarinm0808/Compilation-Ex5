package types;

public class TypeClassVarDecList extends Type
{
	public TypeClassVarDec head;
	public TypeClassVarDecList tail;
	
	public TypeClassVarDecList(TypeClassVarDec head, TypeClassVarDecList tail)
	{
		this.head = head;
		this.tail = tail;
	}	
}
