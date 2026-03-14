package ast;

public class StackOffsetManager {
    private int currentOffset = 0;
    private static StackOffsetManager instance = null;
    
    private StackOffsetManager() {}

    public static StackOffsetManager getInstance() {
        if (instance == null) {
            instance = new StackOffsetManager();
        }
        return instance;
    }
    
    // Call this at the start of every function in AstDecFunc.irMe()
    public void reset(int start) { 
        currentOffset = start; 
    }
    
    // Call this in AstDecVar.irMe() for local variables
    public int getNextOffset() {
        int res = currentOffset;
        currentOffset += 1; // Increment by 1 to stay word-aligned
        return res;
    }

    public int getCount(){
        return currentOffset;
    }
}