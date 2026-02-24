import java.io.*;
import java_cup.runtime.Symbol;
import ast.*;
import ir.*;
import mips.*;

public class Main {
    static public void main(String argv[]) {
        Lexer l;
        Parser p;
        Symbol s;
        AstDecList ast;
        FileReader fileReader;
        PrintWriter fileWriter;

        // Ensure we have the correct number of arguments
        if (argv.length < 2) {
            System.err.println("Usage: java Main <input_file> <output_file>");
            System.exit(1);
        }

        String inputFileName = argv[0];
        String outputFileName = argv[1];

        try {
            /********************************/
            /* [1] Initialize a file reader */
            /********************************/
            fileReader = new FileReader(inputFileName);

            /********************************/
            /* [2] Initialize a file writer */
            /********************************/
            fileWriter = new PrintWriter(outputFileName);

            /******************************************/
            /* [3] Link MipsGenerator to our output   */
            /******************************************/
            // This ensures MipsGenerator writes to the user-specified file
            // instead of the hardcoded default.
            MipsGenerator.getInstance().setPrintWriter(fileWriter);

            /******************************/
            /* [4] Initialize the Lexer   */
            /******************************/
            l = new Lexer(fileReader);

            /*******************************/
            /* [5] Initialize the Parser  */
            /*******************************/
            p = new Parser(l);

            /***********************************/
            /* [6] 3 ... 2 ... 1 ... Parse !!! */
            /***********************************/
            ast = (AstDecList) p.parse().value;

            /*************************/
            /* [7] Print the AST ... */
            /*************************/
            ast.printMe();

            /**********************************/
            /* [8] Semantic Analysis (Types)  */
            /**********************************/
            ast.semantMe();

            /**********************************/
            /* [9] IR Generation              */
            /**********************************/
            // This fills the Ir singleton with commands
            ast.irMe();

            /***************************************************/
            /* [10] MIPS Generation & Register Allocation      */
            /***************************************************/
            // This triggers the per-function liveness analysis,
            // 10-register coloring, and MIPS emission.
            Ir.getInstance().mipsMe();

            /**************************************/
            /* [11] Finalize AST Graphviz file    */
            /**************************************/
            AstGraphviz.getInstance().finalizeFile();

            /***************************************/
            /* [12] Finalize MIPS file (Epilogue)  */
            /***************************************/
            // Prints the exit syscall and closes the fileWriter
            MipsGenerator.getInstance().finalizeFile();

            System.out.println("Compilation successful: " + outputFileName);

        } catch (RuntimeException e) {
            // This catches your "Register Allocation Failed" or "Semantic Error"
            System.err.println(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}