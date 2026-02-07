package types;

public class TypeArray extends Type {
    public Type elementType; // The type of elements (int, string, or a Class)
    public String name;      // The name given in the typedef (e.g., "IntArray")

    public TypeArray(Type elementType, String name) {
        this.elementType = elementType;
        this.name = name;
    }

    @Override
    public String toString() {
        return name != null ? name : (elementType.toString() + "[]");
    }
}