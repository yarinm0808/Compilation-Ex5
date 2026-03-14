# Compiler Construction - Exercise 5
## L-Language: From AST to MIPS Assembly

This project implements the backend of a compiler for the **L Language**. It bridges the gap between high-level language constructs and executable MIPS assembly, focusing on **saturation arithmetic**, **runtime safety**, and **linear scan register allocation**.

## 🚀 Key Features

### 1. Robust IR & MIPS Generation
* **AST to IR Lowering:** Recursively traverses the AST to generate a sequential stream of Intermediate Representation (IR) commands.
* **Register Allocation:** Implements a liveness-aware register allocator that manages a strict pool of 10 physical registers (`$t0–$t9`).
* **Saturation Arithmetic:** Implements L-specific integer logic where all arithmetic results ($+$, $-$, $*$, $/$) are clamped between $-32,768$ and $32,767$ to prevent standard overflow.

### 2. Mandatory Runtime Safety
The compiler injects MIPS-level guards to ensure program stability:
* **Invalid Pointer Dereference:** Checks for `nil` access before any class field or array subscript operation.
* **Illegal Division By Zero:** Validates the denominator before every division operation.
* **Access Violation:** Performs array bounds checking to ensure indices are $\ge 0$ and $<$ array length.

### 3. Sophisticated Memory Management
* **Class & Array Support:** Handles complex memory offsets for class fields and heap allocation (via `malloc` / Syscall 9) for new objects and arrays.
* **String Operations:** Supports string literals and concatenation via heap-allocated memory.

## 📂 Architecture

| Component | Responsibility |
| :--- | :--- |
| `src/ast/` | Recursive AST traversal and `irMe()` implementation. |
| `src/ir/` | Linear command set (Binops, Jumps, Calls, Runtime Checks). |
| `src/mips/` | Translates IR commands into architecture-specific MIPS instructions. |
| `src/reg_alloc/` | Liveness analysis and interference graph management for `$t0-$t9`. |



## 🛠️ Build & Run

### Prerequisites
* Java JDK 8+
* `java-cup-11b-runtime.jar`
* MARS or SPIM (for executing the resulting `.asm` file)

### Compilation
1. **Generate Lexer and Parser:**
   ```bash
   java -jar jflex.jar lexer.jflex
   java -jar java-cup.jar parser.cup