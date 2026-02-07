package types;

public class TypeArray extends Type
{
    public Type type; // The type of elements in the array (e.g., int, string)
    public TypeInt length;
    
    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    
    // NEW: Constructor without name (for anonymous arrays or simple usage)
    public TypeArray(Type type, TypeInt length)
    {
        this.type =type;
        this.length = length;
    }

    // Optional: Useful for debugging errors
    @Override
    public String toString() {
        return type.toString() + "[]";
    }
}