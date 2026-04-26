package types;

public abstract class Type
{
	/******************************/
	/*  Every type has a name ... */
	/******************************/
	public String name;

	/*************/
	/* isClass() */
	/*************/
	public boolean isClass(){ return false;}

	/*************/
	/* isArray() */
	/*************/
	public boolean isArray(){ return false;}

	public boolean isCompatible(Type source) {
        // 1. Identity check
        if (this == source) return true;
        if (this.name != null && this.name.equals(source.name)) return true;

        // 2. Nil Assignment (The "Boss" fix)
        // Nil can be assigned to any Class OR any Array.
        if (source instanceof TypeNil) {
            return (this instanceof TypeClass || this instanceof TypeArray);
        }

        // 3. Array Compatibility
        if (this instanceof TypeArray && source instanceof TypeArray) {
            TypeArray targetArray = (TypeArray) this;
            TypeArray sourceArray = (TypeArray) source;
            // Use recursion! An array of X is compatible with an array of Y 
            // ONLY if X is exactly the same as Y (Arrays are usually invariant).
            return targetArray.elementType.isCompatible(sourceArray.elementType) && 
                   sourceArray.elementType.isCompatible(targetArray.elementType);
        }

        // 4. Class Inheritance (The "Upcasting" fix)
        if (this instanceof TypeClass && source instanceof TypeClass) {
            return ((TypeClass) this).isMyChild((TypeClass) source);
        }

        return false;
    }
}
