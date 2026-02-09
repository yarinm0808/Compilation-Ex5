package ast;

public class ControlFlowContext {
    /************************************************************/
    /* The label representing the exit point of the current function */
    /************************************************************/
    private String currentFunctionEndLabel;

    /**************************************/
    /* USUAL SINGLETON IMPLEMENTATION ... */
    /**************************************/
    private static ControlFlowContext instance = null;

    protected ControlFlowContext() {}

    public static ControlFlowContext getInstance() {
        if (instance == null) {
            instance = new ControlFlowContext();
        }
        return instance;
    }

    public void setCurrentFunctionEndLabel(String label) {
        this.currentFunctionEndLabel = label;
    }

    public String getCurrentFunctionEndLabel() {
        return this.currentFunctionEndLabel;
    }
}