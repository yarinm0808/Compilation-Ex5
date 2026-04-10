package types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public int findFieldOffset(String fieldName) {
        // 1. If we have a father, check if the field belongs to him first
        if (father != null) {
            Type fatherType = father.findFieldType(fieldName);
            // Only return if it's a data field, not a method!
            if (fatherType != null && !(fatherType instanceof TypeFunction)) {
                return father.findFieldOffset(fieldName);
            }
        }

        // 2. If it's not in the father, it's in this class.
        // The offset starts AFTER all of the father's data fields.
        int offset = 4; // VMT is at 0
        if (father != null) {
            offset += (father.getFieldCount() * 4);
        }

        // 3. Iterate through local members. 
        // IMPORTANT: Use a list that matches the DECLARATION order.
        for (TypeList it = data_members; it != null; it = it.tail) {
            if (it.head instanceof TypeClassVarDec) {
                TypeClassVarDec vd = (TypeClassVarDec) it.head;
                if (vd.name.equals(fieldName)) {
                    return offset;
                }
                offset += 4;
            }
        }
        return -1;
    }

    // 2. Count unique methods in the hierarchy to determine VMT size
    public int getMethodCount() {
        Set<String> methodNames = new HashSet<>();
        TypeClass curr = this;
        while (curr != null) {
            for (TypeList it = curr.data_members; it != null; it = it.tail) {
                if (it.head instanceof TypeFunction) {
                    methodNames.add(((TypeFunction) it.head).name);
                }
            }
            curr = curr.father;
        }
        return methodNames.size();
    }

    // 3. Find the fixed index of a method in the VMT
    // This index MUST be the same for the parent and all children
    public int findMethodOffset(String methodName) {
        // We build a list of all unique methods starting from the root (Grandfather)
        List<String> vmtOrder = getVMTOrder();
        for (int i = 0; i < vmtOrder.size(); i++) {
            if (vmtOrder.get(i).equals(methodName)) {
                return i * 4; // Return byte offset in the VMT
            }
        }
        return -1;
    }

    public List<String> getVMTOrder() {
        List<String> order = (father != null) ? father.getVMTOrder() : new ArrayList<>();
        for (TypeList it = data_members; it != null; it = it.tail) {
            if (it.head instanceof TypeFunction) {
                String mName = ((TypeFunction) it.head).name;
                if (!order.contains(mName)) {
                    order.add(mName);
                }
            }
        }
        System.out.println(">> [VMT] Class " + name + " order: " + order);
        return order;
    }
}