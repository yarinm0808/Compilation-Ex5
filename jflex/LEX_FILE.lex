/***************************/
/* FILE NAME: LEX_FILE.lex */
/***************************/

import java_cup.runtime.*;

%%

%public
%class Lexer
%unicode
%cupsym TokenNames
%cup
%char
%line
%column
%function next_token
%type java_cup.runtime.Symbol

/****************/
/* DECLARATIONS */
/****************/

LETTER      = [A-Za-z]
DIGIT       = [0-9]
WHITESPACE  = [ \t\r\n]+
ID          = {LETTER}({LETTER}|{DIGIT})*
INTLIT      = 0|([1-9]{DIGIT}*)
VALID_STR   = \"{LETTER}*\"
CCHAR       = {LETTER}|{DIGIT}|[ \t\r\n]|[\(\)\[\]\{\}\?\!\+\-\*\/\.\;]

%state BLOCK_COMMENT

%{
  private Symbol symbol(int type) {
    return new Symbol(type, yyline+1, yycolumn+1);
  }
  private Symbol symbol(int type, Object val) {
    return new Symbol(type, yyline+1, yycolumn+1, val);
  }
  public int getLine() { return yyline + 1; }
  public int getTokenStartPosition() { return yycolumn + 1; }
  private void lexError(String msg) {
    throw new RuntimeException("ERROR");
  }

  private int parseAndValidateInt(String text) {
    if (text.length() > 1 && text.charAt(0) == '0') lexError("leading zero");
    try {
      long v = Long.parseLong(text);
      if (v < 0 || v > 32767L) lexError("out of range");
      return (int)v;
    } catch (NumberFormatException e) {
      lexError("bad int");
      return 0;
    }
  }
%}

%%

<YYINITIAL> {

  {WHITESPACE}                { /* skip */ }

  // Type-1 comment
  "//".*[\r]?\n               { /* skip */ }

  // Type-2 comment: /* ... */
  "/*"                        { yybegin(BLOCK_COMMENT); }

  // Operators & punctuation
  ":="                        { return symbol(TokenNames.ASSIGN); }
  "="                         { return symbol(TokenNames.EQ); }
  "<"                         { return symbol(TokenNames.LT); }
  ">"                         { return symbol(TokenNames.GT); }
  "("                         { return symbol(TokenNames.LPAREN); }
  ")"                         { return symbol(TokenNames.RPAREN); }
  "["                         { return symbol(TokenNames.LBRACK); }
  "]"                         { return symbol(TokenNames.RBRACK); }
  "{"                         { return symbol(TokenNames.LBRACE); }
  "}"                         { return symbol(TokenNames.RBRACE); }
  ","                         { return symbol(TokenNames.COMMA); }
  "."                         { return symbol(TokenNames.DOT); }
  ";"                         { return symbol(TokenNames.SEMICOLON); }
  "+"                         { return symbol(TokenNames.PLUS); }
  "-"                         { return symbol(TokenNames.MINUS); }
  "*"                         { return symbol(TokenNames.TIMES); }
  "/"                         { return symbol(TokenNames.DIVIDE); }

  // Keywords — must come BEFORE ID!
  "array"                     { return symbol(TokenNames.ARRAY); }
  "class"                     { return symbol(TokenNames.CLASS); }
  "return"                    { return symbol(TokenNames.RETURN); }
  "while"                     { return symbol(TokenNames.WHILE); }
  "if"                        { return symbol(TokenNames.IF); }
  "else"                      { return symbol(TokenNames.ELSE); }
  "new"                       { return symbol(TokenNames.NEW); }
  "extends"                   { return symbol(TokenNames.EXTENDS); }
  "nil"                       { return symbol(TokenNames.NIL); }
  "int"                       { return symbol(TokenNames.TYPE_INT); }
  "string"                    { return symbol(TokenNames.TYPE_STRING); }
  "void"                      { return symbol(TokenNames.TYPE_VOID); }

  // Literals & IDs
  {INTLIT}                    { return symbol(TokenNames.INT, parseAndValidateInt(yytext())); }
  {VALID_STR}                 { 
                                String s = yytext().substring(1, yytext().length()-1); 
                                return symbol(TokenNames.STRING, s); 
                              }
  "\""[^\"]*                  { lexError("unclosed string"); }
  {ID}                        { return symbol(TokenNames.ID, yytext()); }

  <<EOF>>                     { return symbol(TokenNames.EOF); }

  .                           { lexError("invalid char"); }
}

<BLOCK_COMMENT> {
    "*/"         { yybegin(YYINITIAL); }
    .|\n         { /* stay */ }
    <<EOF>>      { lexError("unclosed block comment"); }
}
