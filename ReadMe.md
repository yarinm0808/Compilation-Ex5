# Compiler Construction - Exercise 5
## Abstract Syntax Tree & Intermediate Representation

This project implements the frontend and intermediate representation (IR) phases of a compiler for the **L Language**. It focuses on building a robust Abstract Syntax Tree (AST), performing semantic analysis with a scoped Symbol Table, and generating a linear IR.

## 🚀 Features

- **AST Hierarchy:** A clean, object-oriented AST design using abstract base classes for Nodes, Expressions, Statements, and Types.
- **Semantic Analysis:** - Full type checking and variable resolution.
    - Scoped Symbol Table using a chained hash table with support for **shadowing**.
    - Detection of double declarations and undeclared variables.
- **IR Generation:** Translates AST nodes into a sequence of IR commands (Allocate, Load, Store, Binop, etc.) using a temporary register allocation scheme.
- **Error Reporting:** Precise error reporting following the `ERROR(line)` format required by the course specifications.

## 📂 Project Structure

- `src/ast/`: Contains all AST node definitions (Declarations, Expressions, Statements).
- `src/symboltable/`: Implementation of the scoped Symbol Table and Entry logic.
- `src/types/`: Type system representing primitives (int, string, void) and complex types (classes, arrays).
- `src/ir/`: The intermediate representation command set and singleton generator.
- `src/temp/`: Temporary register management.
- `cup/`: Grammar specification (`parser.cup`).
- `jflex/`: Lexical specification (`lexer.jflex`).

## 🛠️ Build & Run

To build the compiler, ensure you have `java-cup` and `jflex` in your classpath.

1. **Generate Lexer and Parser:**
   ```bash
   java -jar jflex-1.x.x.jar lexer.jflex
   java -jar java-cup-11b.jar parser.cup