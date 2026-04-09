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

	// Inside Type.java
	public boolean isCompatible(Type source) {
		// 1. "Am I the same object as the source?"
		if (this == source) return true;

		// 2. "Am I a Class or Array, and is the source Nil?"
		if (source instanceof TypeNil) {
			return (this instanceof TypeClass || this instanceof TypeArray);
		}

		// 3. "Am I an Array and is the source an Array with the same element?"
		if (this instanceof TypeArray && source instanceof TypeArray) {
			TypeArray targetArray = (TypeArray) this;
			TypeArray sourceArray = (TypeArray) source;
			return targetArray.elementType == sourceArray.elementType;
		}

		// 4. "Am I a Class and is the source my child (Inheritance)?"
		if (this instanceof TypeClass && source instanceof TypeClass) {
			return ((TypeClass) this).isMyChild((TypeClass) source);
		}

		return false;
	}
}
