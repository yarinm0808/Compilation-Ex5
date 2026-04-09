package types;

public class TypeClass extends Type {
    public String name;
    public TypeClass father;
    public TypeList data_members;

    public TypeClass(TypeClass father, String name, TypeList data_members) {
        this.name = name;
        this.father = father;
        this.data_members = data_members;
    }

    public Type findFieldType(String fieldName) {
        System.out.println(">> [DEBUG] Searching for '" + fieldName + "' in class: " + name);
        for (TypeList it = data_members; it != null; it = it.tail) {
            if (it.head instanceof TypeClassVarDec) {
                TypeClassVarDec vd = (TypeClassVarDec) it.head;
                if (vd.name.equals(fieldName)) {
                    System.out.println("   [MATCH] Field '" + fieldName + "' found in " + name);
                    return vd.t;
                }
            }
            if (it.head instanceof TypeFunction) {
                TypeFunction tf = (TypeFunction) it.head;
                if (tf.name.equals(fieldName)) {
                    System.out.println("   [MATCH] Method '" + fieldName + "' found in " + name);
                    return tf;
                }
            }
        }
        if (father != null){
            System.out.println("   [NOT FOUND] climbing to father: " + father.name);
            return father.findFieldType(fieldName);
        }
        
        System.out.println("   [FAIL] '" + fieldName + "' not found in hierarchy.");
        return null;
    }
    
	public int getFieldCount() {
		int count = 0;
		
		for (TypeList it = data_members; it != null; it = it.tail) {
			// Only count if the member is a data field, not a method
			if (!(it.head instanceof TypeFunction)) {
				count++;
			}
		}

		if (father != null) {
			count += father.getFieldCount();
		}
		return count;
	}

    public int findFieldOffset(String fieldName) {
        int offset = 0;

        // 1. If we have a father, his fields come FIRST in memory
        if (father != null) {
            int fatherFieldCount = father.getFieldCount();
            // Check if the field is in the father's memory space
            Type fatherType = father.findFieldType(fieldName);
            if (fatherType != null) {
                return father.findFieldOffset(fieldName);
            }
            // If not in father, start our local offsets after the father's fields
            offset = fatherFieldCount * 4;
        }

        // 2. Search locally in our own members
        for (TypeList it = data_members; it != null; it = it.tail) {
            if (it.head instanceof TypeClassVarDec) {
                TypeClassVarDec vd = (TypeClassVarDec) it.head;
                if (vd.name.equals(fieldName)) {
                    return offset;
                }
                offset += 4; // Each field takes 4 bytes
            }
        }
        return -1; // Should not happen if semantMe passed
    }

    /**
     * Checks if 'potentialChild' is a subclass of 'this' (the current class).
     */
    public boolean isMyChild(TypeClass potentialChild) {
        TypeClass curr = potentialChild;
        while (curr != null) {
            // Check by name or reference
            if (curr.name.equals(this.name)) return true;
            curr = curr.father;
        }
        return false;
    }
}