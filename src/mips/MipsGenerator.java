/***********/
/* PACKAGE */
/***********/
package mips;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.io.PrintWriter;
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
	public void finalizeFile()
	{
		fileWriter.print("\tli $v0,10\n");
		fileWriter.print("\tsyscall\n");
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
	public void allocate(String varName)
	{
		fileWriter.format(".data\n");
		fileWriter.format("\tglobal_%s: .word 721\n",varName);
	}
	public void load(String dstReg, String varName, int offset) {
		if (varName == null) {
			// It's a Local or a Parameter
			fileWriter.format("\tlw %s, %d($sp)\n", dstReg, offset);
		} else {
			// It's a Global variable
			fileWriter.format("\tlw %s, %s\n", dstReg, varName);
		}
	}
	public void store(String srcReg, String varName, int offset) {
		if (varName == null) {
			fileWriter.format("\tsw %s, %d($sp)\n", srcReg, offset);
		} else {
			fileWriter.format("\tsw %s, %s\n", srcReg, varName);
		}
	}
	public void li(String idx, int value)
	{
		fileWriter.format("\tli %s,%d\n",idx,value);
	}

	public void ReturnValue(String retval){
		fileWriter.format("\tmove $v0, %s\n", retval);
	}
	public void JumpReturn(){
		fileWriter.format("\tjr $ra\n");
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

	public void prologue() {
		// 1. Move stack pointer down by 44 bytes
		fileWriter.format("\taddi $sp, $sp, -44\n");
		
		// 2. Save the Return Address
		fileWriter.format("\tsw $ra, 40($sp)\n");
		
		// 3. Save all 10 temporary registers
		for (int i = 0; i <= 9; i++) {
			fileWriter.format("\tsw $t%d, %d($sp)\n", i, i * 4);
		}
	}

	public void epilogue() {
		// 1. Restore all 10 temporary registers
		for (int i = 0; i <= 9; i++) {
			fileWriter.format("\tlw $t%d, %d($sp)\n", i, i * 4);
		}
		
		// 2. Restore the Return Address
		fileWriter.format("\tlw $ra, 40($sp)\n");
		
		// 3. Move stack pointer back up
		fileWriter.format("\taddi $sp, $sp, 44\n");
	}

	public void add(String dst, String oprnd1, String oprnd2)
	{
		String upperCheck = "upper_" + AstNodeSerialNumber.getFresh();
		String done = "done_" + AstNodeSerialNumber.getFresh();

		fileWriter.format("\taddu %s,%s,%s\n", dst, oprnd1, oprnd2);
		
		// Check upper bound: 32767 
		fileWriter.format("\tli $at, 32767\n");
		fileWriter.format("\tble %s, $at, %s\n", dst, upperCheck);
		fileWriter.format("\tmove %s, $at\n", dst);
		fileWriter.format("\tj %s\n", done);

		fileWriter.format("%s:\n", upperCheck);
		// Check lower bound: -32768 
		fileWriter.format("\tli $at, -32768\n");
		fileWriter.format("\tbge %s, $at, %s\n", dst, done);
		fileWriter.format("\tmove %s, $at\n", dst);

		fileWriter.format("%s:\n", done);
	}
	public void add(String command) {
    // Simply print the string with a tab for formatting
    fileWriter.format("\t%s\n", command);
}

	public void sub(String dstidx, String i1, String i2){

		String label_upper_check = "label_sub_upper_" + AstNodeSerialNumber.getFresh();
    	String label_done = "label_sub_done_" + AstNodeSerialNumber.getFresh();

		fileWriter.format("\tsubu %s,%s,%s\n",dstidx,i1,i2);
		fileWriter.format("\tli $at, 32767\n");
		fileWriter.format("\tble %s, $at, %s\n",dstidx, label_upper_check);
		fileWriter.format("\tmove %s, $at\n", dstidx);
		fileWriter.format("\tj %s", label_done);

		fileWriter.format("%s:\n",label_upper_check);

		fileWriter.format("\tli $at, -32768\n");
		fileWriter.format("\tbge %s, $at, %s\n",dstidx, label_upper_check);
		fileWriter.format("\tmove %s, $at\n", dstidx);
		fileWriter.format("\tj %s", label_done);
		fileWriter.format("%s:\n",label_done);
	}
	
	public void mul(String dstidx, String i1, String i2)
	{
		String label_upper_check = "label_mul_upper_" + AstNodeSerialNumber.getFresh();
		String label_done = "label_mul_done_" + AstNodeSerialNumber.getFresh();

		// 1. Perform multiplication
		fileWriter.format("\tmul %s,%s,%s\n", dstidx, i1, i2);

		// 2. Upper Bound Check (32767)
		fileWriter.format("\tli $at, 32767\n");
		fileWriter.format("\tble %s, $at, %s\n", dstidx, label_upper_check);
		fileWriter.format("\tmove %s, $at\n", dstidx);
		fileWriter.format("\tj %s\n", label_done); 

		// 3. Lower Bound Check (-32768)
		fileWriter.format("%s:\n", label_upper_check);
		fileWriter.format("\tli $at, -32768\n"); // Corrected to -32768
		fileWriter.format("\tbge %s, $at, %s\n", dstidx, label_done); // Jump to done
		fileWriter.format("\tmove %s, $at\n", dstidx);

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
		fileWriter.format("\tli $at, 32767\n");
		fileWriter.format("\tble %s, $at, %s\n", d, label_upper_check);
		fileWriter.format("\tmove %s, $at\n", d);
		fileWriter.format("\tj %s\n", label_done);

		// 3. Saturation: Check Lower Bound (-32768)
		fileWriter.format("%s:\n", label_upper_check);
		fileWriter.format("\tli $at, -32768\n");
		fileWriter.format("\tbge %s, $at, %s\n", d, label_done);
		fileWriter.format("\tmove %s, $at\n", d);

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
	public void label(String inlabel)
	{
		if (inlabel.equals("main"))
		{
			fileWriter.format(".text\n"); 
			fileWriter.format("%s:\n",inlabel);
		}
		else
		{
			fileWriter.format("%s:\n",inlabel);
		}
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
	public void beqz(String i1, String label)
	{			
		fileWriter.format("\tbeqz %s,$zero,%s\n",i1,label);				
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
		}
		return instance;
	}
}
