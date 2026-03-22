/***********/
/* PACKAGE */
/***********/
package mips;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ast.AstNodeSerialNumber;
/*******************/
/* PROJECT IMPORTS */
/*******************/
import temp.*;

public class MipsGenerator
{
	private static final int WORD_SIZE=4;
	/***********************/
	/* The file writer ... */
	/***********************/
	private PrintWriter fileWriter;

	/***********************/
	/* The file writer ... */
	/***********************/
	public void finalizeFile() {
		fileWriter.print("\nmain:\n");
		fileWriter.print("\tjal func_main\n");
		fileWriter.print("\tli $v0, 10\n");
		fileWriter.print("\tsyscall\n");
		fileWriter.flush(); // Force write to disk
		fileWriter.close();
	}
	public void printInt(String idx)
	{
		// fileWriter.format("\taddi $a0,Temp_%d,0\n",idx);
		fileWriter.format("\tmove $a0,%s\n",idx);
		fileWriter.format("\tli $v0,1\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $a0,32\n");
		fileWriter.format("\tli $v0,11\n");
		fileWriter.format("\tsyscall\n");
	}
	public void moveA0(String srcReg) {
		// This moves the value into the physical argument register
		fileWriter.format("\tmove $a0, %s\n", srcReg);
	}
	public void storeField(String srcReg, String baseReg, int offset) {
    	// Generates the correct store syntax: sw $value, offset($base)
    	fileWriter.format("\tsw %s, %d(%s)\n", srcReg, offset, baseReg);
	}
	public void call(String physicalDst, String funcName, List<String> physicalArgs) {
		// 1. Push arguments onto the stack
		if (physicalArgs != null && !physicalArgs.isEmpty()) {
			int stackSpace = physicalArgs.size() * 4;
			fileWriter.format("\tsubiu $sp, $sp, %d\n", stackSpace);
			
			for (int i = 0; i < physicalArgs.size(); i++) {
				String regName = physicalArgs.get(i);
				// We use the physical register name directly now
				fileWriter.format("\tsw %s, %d($sp)\n", regName, i * 4);
			}
		}

		// 2. Perform the jump and link
		fileWriter.format("\tjal %s\n", funcName);

		// 3. Clean up the stack
		if (physicalArgs != null && !physicalArgs.isEmpty()) {
			int stackSpace = physicalArgs.size() * 4;
			fileWriter.format("\taddiu $sp, $sp, %d\n", stackSpace);
		}

		// 4. Retrieve the return value from $v0
		if (physicalDst != null) {
			// Move the result from MIPS return register $v0 to our allocated physical register
			fileWriter.format("\tmove %s, $v0\n", physicalDst);
		}
	}
//	public Temp addressLocalVar(int serialLocalVarNum)
//	{
//		Temp t  = TempFactory.getInstance().getFreshTemp();
//		int idx = t.getSerialNumber();
//
//		fileWriter.format("\taddi Temp_%d,$fp,%d\n",idx,-serialLocalVarNum*WORD_SIZE);
//
//		return t;
//	}
	public void allocate(String varName) {
		fileWriter.println("\n.data");               // Flicker to data
		fileWriter.format("%s: .word 721\n", varName); // Drop variable
		fileWriter.println(".text");                 // Flicker back to text
		fileWriter.flush();
	}

	public void malloc(String addrReg, int size){
		fileWriter.format("\tli $a0, %d\n", size);
		fileWriter.format("\tli $v0, 9\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\t move %s, $v0\n", addrReg);
	}
	public void store(String srcReg, String varName, int offset) {
		if (varName == null || "null".equals(varName)) {
			// Just use the offset directly. 
			// If the AST sent -44, we print -44.
			fileWriter.format("\tsw %s, %d($fp)\n", srcReg, offset);
		} else {
			fileWriter.format("\tsw %s, %s\n", srcReg, varName);
		}
	}
	public void load(String destReg, String varName, int offset) {
		if (varName == null || "null".equals(varName)) {
			// CASE 1: Local Stack Variable
			fileWriter.format("\tlw %s, %d($fp)\n", destReg, offset);
		} 
		else if (varName.startsWith("$")) {
			// CASE 2: Register-Relative (Field Access/Array Access)
			// This generates: lw $t0, 0($t1)
			fileWriter.format("\tlw %s, %d(%s)\n", destReg, offset, varName);
		} 
		else {
			// CASE 3: Global Variable Label
			fileWriter.format("\tlw %s, %s\n", destReg, varName);
		}
	}
	public void li(String idx, int value)
	{
		fileWriter.format("\tli %s,%d\n",idx,value);
	}

	public void ReturnValue(String retval, String exitLabel) {
		fileWriter.format("\tmove $v0, %s\n", retval);
		fileWriter.format("\tj %s\n", exitLabel); 
	}
	public void JumpReturn(){
		fileWriter.format("\tjr $ra\n");
	}

	public void Move(String DstReg, String target){
        fileWriter.format("\tmove %s, %s\n", DstReg, target);
    }

	public void push(String ToPush){
		fileWriter.format("\taddi $sp, $sp, -4\n");
		fileWriter.format("\tsw %s, 0($sp)\n", ToPush);
	}

	public void func_call(String FuncName){
		fileWriter.format("\tjal %s\n", FuncName);
	}

	public void StackPointerUpdate(int bytes){
		fileWriter.format("\taddu $sp, $sp, %d\n", bytes);
	}

	public void GetRetVal(String targetReg){
		fileWriter.format("\tmove %s, $v0\n", targetReg);
	}

	public void prologue(int localStackSize) {
		fileWriter.format("\tsubu $sp, $sp, 4\n");
		fileWriter.format("\tsw $ra, 0($sp)\n");
		fileWriter.format("\tsubu $sp, $sp, 4\n");
		fileWriter.format("\tsw $fp, 0($sp)\n");
		fileWriter.format("\tmove $fp, $sp\n");
		
		for (int i = 0; i <= 9; i++) {
			fileWriter.format("\tsubu $sp, $sp, 4\n");
			fileWriter.format("\tsw $t%d, 0($sp)\n", i);
		}
		
		// Use the dynamic size calculated by the AST!
		fileWriter.format("\tsubu $sp, $sp, %d\n", localStackSize);
	}

	public void epilogue(int bytesNeeded) {
		// 1. Reset SP to the base of the frame
		fileWriter.println("\tmove $sp, $fp");

		// 2. Restore $t0-$t9 (They were saved at -4, -8 ... -40 relative to the OLD SP)
		// But since we moved SP to FP, and FP was at -4 relative to the start...
		// The simplest way is to use the offsets from the current $sp (which is $fp)
		for (int i = 0; i <= 9; i++) {
			fileWriter.format("\tlw $t%d, %d($sp)\n", i, -(i * 4 + 4));
		}

		// 3. Restore FP and RA
		fileWriter.println("\tlw $fp, 0($sp)");
		fileWriter.println("\tlw $ra, 4($sp)");
		fileWriter.println("\taddu $sp, $sp, 8");
	}

	public void add(String dst, String oprnd1, String oprnd2)
	{
		String upperCheck = "upper_" + AstNodeSerialNumber.getFresh();
		String done = "done_" + AstNodeSerialNumber.getFresh();

		fileWriter.format("\taddu %s,%s,%s\n", dst, oprnd1, oprnd2);
		
		// Check upper bound: 32767 
		fileWriter.format("\tli $s0, 32767\n");
		fileWriter.format("\tble %s, $s0, %s\n", dst, upperCheck);
		fileWriter.format("\tmove %s, $s0\n", dst);
		fileWriter.format("\tj %s\n", done);

		fileWriter.format("%s:\n", upperCheck);
		// Check lower bound: -32768 
		fileWriter.format("\tli $s0, -32768\n");
		fileWriter.format("\tbge %s, $s0, %s\n", dst, done);
		fileWriter.format("\tmove %s, $s0\n", dst);

		fileWriter.format("%s:\n", done);
	}
	public void add(String command) {
    	// Simply print the string with a tab for formatting
    	fileWriter.format("\t%s\n", command);
	}

	public void sub(String dst, String oprnd1, String oprnd2) {
		System.out.println(String.format("[DEBUG] Generating SUB: %s = %s - %s", dst, oprnd1, oprnd2));
		String upperCheck = "label_sub_upper_" + AstNodeSerialNumber.getFresh();
		String done = "label_sub_done_" + AstNodeSerialNumber.getFresh();

		// 1. Perform Subtraction: dst = oprnd1 - oprnd2
		fileWriter.format("\tsubu %s, %s, %s\n", dst, oprnd1, oprnd2);
		
		// 2. Saturation: Check Upper Bound (32767) 
		fileWriter.format("\tli $v1, 32767\n"); 
		fileWriter.format("\tble %s, $v1, %s\n", dst, upperCheck);
		fileWriter.format("\tmove %s, $v1\n", dst);
		fileWriter.format("\tj %s\n", done);

		// 3. Saturation: Check Lower Bound (-32768) 
		fileWriter.format("%s:\n", upperCheck);
		fileWriter.format("\tli $v1, -32768\n");
		// If dst >= -32768, we are within range, jump to done
		fileWriter.format("\tbge %s, $v1, %s\n", dst, done);
		// Otherwise, clamp to -32768
		fileWriter.format("\tmove %s, $v1\n", dst);

		fileWriter.format("%s:\n", done);
	}
	
	public void mul(String dstidx, String i1, String i2)
	{
		String label_upper_check = "label_mul_upper_" + AstNodeSerialNumber.getFresh();
		String label_done = "label_mul_done_" + AstNodeSerialNumber.getFresh();

		// 1. Perform multiplication
		fileWriter.format("\tmul %s,%s,%s\n", dstidx, i1, i2);

		// 2. Upper Bound Check (32767)
		fileWriter.format("\tli $at, 32767\n");
		fileWriter.format("\tble %s, $s0, %s\n", dstidx, label_upper_check);
		fileWriter.format("\tmove %s, $s0\n", dstidx);
		fileWriter.format("\tj %s\n", label_done); 

		// 3. Lower Bound Check (-32768)
		fileWriter.format("%s:\n", label_upper_check);
		fileWriter.format("\tli $s0, -32768\n"); // Corrected to -32768
		fileWriter.format("\tbge %s, $s0, %s\n", dstidx, label_done); // Jump to done
		fileWriter.format("\tmove %s, $s0\n", dstidx);

		fileWriter.format("%s:\n", label_done);
	}
	public void div(String d, String s, String t) {		
		// Generate unique labels to avoid MIPS assembly errors
		String label_upper_check = "label_div_upper_" + AstNodeSerialNumber.getFresh();
		String label_done = "label_div_done_" + AstNodeSerialNumber.getFresh();

		// 1. Perform Division
		// Note: Standard MIPS div uses lo/hi registers
		fileWriter.format("\tdiv %s, %s\n", s, t);
		fileWriter.format("\tmflo %s\n", d);

		// 2. Saturation: Check Upper Bound (32767)
		// This handles the specific case of -32768 / -1
		fileWriter.format("\tli $s0, 32767\n");
		fileWriter.format("\tble %s, $s0, %s\n", d, label_upper_check);
		fileWriter.format("\tmove %s, $s0\n", d);
		fileWriter.format("\tj %s\n", label_done);

		// 3. Saturation: Check Lower Bound (-32768)
		fileWriter.format("%s:\n", label_upper_check);
		fileWriter.format("\tli $s0, -32768\n");
		fileWriter.format("\tbge %s, $s0, %s\n", d, label_done);
		fileWriter.format("\tmove %s, $s0\n", d);

		fileWriter.format("%s:\n", label_done);
	}

	public void check_null_ptr(String t1) {
		// Unique label to continue execution
		String label_is_valid = "label_null_ptr_ok_" + AstNodeSerialNumber.getFresh();
		// 1. If pointer is NOT null, jump over the error handling
		fileWriter.format("\tbne %s, $zero, %s\n", t1, label_is_valid);
		// 2. Error Handling (executes only if pointer IS null)
		fileWriter.format("\tla $a0, string_invalid_ptr_dref\n"); // Use the correct string
		fileWriter.format("\tli $v0, 4\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $v0, 10\n"); // Exit gracefully
		fileWriter.format("\tsyscall\n");   
		// 3. Label for valid pointer continuation
		fileWriter.format("%s:\n", label_is_valid);
	}

	public void check_array_bounds(String arrReg, String idxReg) {
		String label_ok = "label_bounds_ok_" + AstNodeSerialNumber.getFresh();
		
		// 1. Check if index < 0
		fileWriter.format("\tbltz %s, label_runtime_error\n", idxReg);
		
		// 2. Load array length (assuming length is at offset 0)
		// If your length is at offset -4, use -4(%s)
		fileWriter.format("\tlw $at, 0(%s)\n", arrReg);
		
		// 3. Check if index < length
		fileWriter.format("\tblt %s, $at, %s\n", idxReg, label_ok);
		
		// 4. If we didn't jump to ok, it's an error
		fileWriter.format("\tj label_runtime_error\n");
		
		fileWriter.format("%s:\n", label_ok);
	}

	public void allocateString(String varName, String content) {
		System.out.println("[DEBUG] Writing to File: .data " + varName + " : .asciiz " + content);
		fileWriter.println("\n.data");               // Flicker to data
		String strLabel = varName + "_str";
		fileWriter.format("%s: .asciiz %s\n", strLabel, content);
		fileWriter.format("%s: .word %s\n", varName, strLabel);
		fileWriter.println(".text");                 // Flicker back to text
		fileWriter.flush();
	}
	public void printString(String regName) {
		// 1. Move the address of the string to $a0
		fileWriter.format("\tmove $a0, %s\n", regName);
		
		// 2. Load syscall code 4 (print_string) into $v0
		fileWriter.format("\tli $v0, 4\n");
		
		// 3. Trigger the syscall
		fileWriter.format("\tsyscall\n");
		fileWriter.flush();
	}

	public void label(String inlabel) {
		// We only force .text for the very first function or 
		// leave it out entirely if you already print it in the constructor.
		if (inlabel.startsWith("func_")) {
			fileWriter.print("\n"); // Just a newline for readability
		}
		
		// Print the label at the start of the line, followed by a colon
		fileWriter.print(inlabel + ":\n");
		fileWriter.flush();
	}
	public void jump(String inlabel)
	{
		fileWriter.format("\tj %s\n",inlabel);
	}	
	public void blt(String oprnd1, String oprnd2, String label)
	{
		fileWriter.format("\tblt %s,%s,%s\n",oprnd1,oprnd2,label);				
	}
	public void bge(String i1, String i2, String label)
	{
		fileWriter.format("\tbge %s,%s,%s\n",i1,i2,label);				
	}
	public void bne(String i1, String i2, String label)
	{	
		fileWriter.format("\tbne %s,%s,%s\n",i1,i2,label);				
	}
	public void beq(String i1, String i2, String label)
	{	
		fileWriter.format("\tbeq %s,%s,%s\n",i1,i2,label);				
	}
	public void beqz(String i1, String label) {           
		fileWriter.format("\tbeqz %s, %s\n", i1, label); // Removed $zero
	}
	public void check_division_by_zero(String denominator) {
    	String illegalDivLabel = "label_illegal_div_" + AstNodeSerialNumber.getFresh();
    
    	// If denominator != 0, skip the error handling
    	fileWriter.format("\tbne %s,$zero,%s\n", denominator, illegalDivLabel);
    
    	// Otherwise: Print "Illegal Division By Zero" and exit
    	fileWriter.format("\tla $a0, string_illegal_div_by_0\n");
    	fileWriter.format("\tli $v0, 4\n");
    	fileWriter.format("\tsyscall\n");
    	fileWriter.format("\tli $v0, 10\n");
    	fileWriter.format("\tsyscall\n");
    
    	fileWriter.format("%s:\n", illegalDivLabel);
	}

	public void add_saturated(Temp dst, Temp op1, Temp op2) {
		int d = dst.getSerialNumber();
		int i1 = op1.getSerialNumber();
		int i2 = op2.getSerialNumber();
		String upperCheck = "upper_" + AstNodeSerialNumber.getFresh();
		String done = "done_" + AstNodeSerialNumber.getFresh();

		fileWriter.format("\taddu Temp_%d,Temp_%d,Temp_%d\n", d, i1, i2);
		
		// Check upper bound: 32767 
		fileWriter.format("\tli $at, 32767\n");
		fileWriter.format("\tble Temp_%d, $at, %s\n", d, upperCheck);
		fileWriter.format("\tmove Temp_%d, $at\n", d);
		fileWriter.format("\tj %s\n", done);

		fileWriter.format("%s:\n", upperCheck);
		// Check lower bound: -32768 [cite: 16]
		fileWriter.format("\tli $at, -32768\n");
		fileWriter.format("\tbge Temp_%d, $at, %s\n", d, done);
		fileWriter.format("\tmove Temp_%d, $at\n", d);

		fileWriter.format("%s:\n", done);
	}	
	public void setPrintWriter(PrintWriter pw) {
		this.fileWriter = pw;
		fileWriter.print(".globl main\n");
		fileWriter.print(".data\n");
		fileWriter.print("string_access_violation: .asciiz \"Access Violation\\n\"\n");
		fileWriter.print("string_illegal_div_by_0: .asciiz \"Illegal Division By Zero\\n\"\n");
		fileWriter.print("string_invalid_ptr_dref: .asciiz \"Invalid Pointer Dereference\\n\"\n");
		fileWriter.print(".text\n"); // Start the text segment immediately
	}
	
	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static MipsGenerator instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected MipsGenerator() {}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static MipsGenerator getInstance()
	{
		if (instance == null)
		{
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new MipsGenerator();

			try
			{
				/*********************************************************************************/
				/* [1] Open the MIPS text file and write data section with error message strings */
				/*********************************************************************************/
				String dirname="./output/";
				String filename=String.format("MIPS.txt");

				/***************************************/
				/* [2] Open MIPS text file for writing */
				/***************************************/
				instance.fileWriter = new PrintWriter(dirname+filename);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			/*****************************************************/
			/* [3] Print data section with error message strings */
			/*****************************************************/
			instance.fileWriter.print(".data\n");
			instance.fileWriter.print("string_access_violation: .asciiz \"Access Violation\"\n");
			instance.fileWriter.print("string_illegal_div_by_0: .asciiz \"Illegal Division By Zero\"\n");
			instance.fileWriter.print("string_invalid_ptr_dref: .asciiz \"Invalid Pointer Dereference\"\n");
			instance.fileWriter.println(".text");
		}
		return instance;
	}
}
