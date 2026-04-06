package types;

public class TypeNil extends Type {
    private static TypeNil instance = null;
    public static TypeNil getInstance() {
        if (instance == null) instance = new TypeNil();
        return instance;
    }
    // nil is only compatible with classes and arrays
}