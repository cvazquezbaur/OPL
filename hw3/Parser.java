/**
 * Author: Carlos Andres Vazquez Baur
 * Homework: #3
 * File: Parser.java
 * 
 * Recursive descent parser implementation for MyPL. The parser
 * requires a lexer. Once a parser is created, the parse() method
 * ensures the given program is syntactically correct. 
 */

import java.sql.Driver;
import java.util.*;


public class Parser {
    
  private Lexer lexer; 
  private Token currToken = null;
  private boolean debug_flag = false;  // set to false to remove debug comments
  
  /** 
   * Create a new parser over the given lexer.
   */
  public Parser(Lexer lexer) {
    this.lexer = lexer;
  }

  /**
   * Ensures program is syntactically correct. On error, throws a
   * MyPLException.
   */
  public void parse() throws MyPLException
  {
    advance();
    stmts();
    eat(TokenType.EOS, "expecting end of file");
  }


  /* Helper Functions */

  // sets current token to next token in stream
  private void advance() throws MyPLException {
    currToken = lexer.nextToken();
  }

  // checks that current token matches given type and advances,
  // otherwise creates an error with the given error message
  private void eat(TokenType t, String errmsg) throws MyPLException {
    if (currToken.type() == t)
      advance();
    else
      error(errmsg);
  }

  // generates an error message from the given message and throws a
  // corresponding MyPLException
  private void error(String errmsg) throws MyPLException {
    String s = errmsg + " found '" + currToken.lexeme() + "'";
    int row = currToken.row();
    int col = currToken.column();
    throw new MyPLException("Parser", errmsg, row, col);
  }

  // function to print a debug string if the debug_flag is set for
  // helping to diagnose/test the parser
  private void debug(String msg) {
    if (debug_flag)
      System.out.println(msg);
  }

  
  /* Recursive Descent Functions */
  //handling of epsilons will be done by recursing all the way down and not eating
  //a token unless it can be handled by a simple if statement


  // <stmts> ::= <stmt> <stmts> | epsilon
  private void stmts() throws MyPLException {
    debug("<stmts>");
    if(currToken.type() != TokenType.EOS){
      stmt();
      stmts();
    }
  }
////////////////////////////////////////////////////////////////////////////
//all of my helper functions

   //helper function to determine if something is an expression
  private boolean isExpression(TokenType token) {
    Set<TokenType> b = Set.of(TokenType.NOT, TokenType.LPAREN, TokenType.NIL, 
    TokenType.NEW, TokenType.NEG, TokenType.ID);
    if(isPval(token) || b.contains(token)){
      return true;
    } else {
      return false;
    }
  }

  //helper function to determine if something is an rval in an expression
  private boolean isRval(TokenType token) {
    Set<TokenType> s = Set.of(TokenType.NIL, TokenType.NEW, TokenType.NEG, TokenType.ID);
    if(isPval(token) || s.contains(token)){
      return true;
    } else {
      return false;
    }
  }

   //helper function for types
  private boolean isDtype(TokenType token){
    Set<TokenType> s = Set.of(TokenType.INT_TYPE, TokenType.DOUBLE_TYPE, TokenType.STRING_TYPE,
    TokenType.BOOL_TYPE, TokenType.CHAR_TYPE);
    return(s.contains(token));
  }

  //helper function for bstmt
  private boolean isBstmt(TokenType token){
    Set<TokenType> s = Set.of(TokenType.VAR, TokenType.SET, 
    TokenType.IF, TokenType.FOR, TokenType.RETURN);
    if(isExpression(currToken.type()) || s.contains(token)){
      return true;
    } else {
      return false;
    }
  }

   //helper function for determing if token is an operator
  private boolean isOperator(TokenType token) { 
    Set<TokenType> s = Set.of(TokenType.PLUS, TokenType.MINUS, TokenType.DIVIDE, TokenType.MULTIPLY, 
    TokenType.MODULO, TokenType.AND, TokenType.OR, TokenType.EQUAL, TokenType.LESS_THAN, 
    TokenType.LESS_THAN_EQUAL, TokenType.GREATER_THAN, TokenType.GREATER_THAN_EQUAL, TokenType.NOT_EQUAL);
    return s.contains(token);
  }

   //helper function for pvalues
  private boolean isPval(TokenType token) { 
    Set<TokenType> s = Set.of(TokenType.INT_VAL,TokenType.DOUBLE_VAL, 
    TokenType.BOOL_VAL, TokenType.STRING_VAL, TokenType.CHAR_VAL);
    return s.contains(token);
  }
////////////////////////////////////////////////////////////////////////

  //<stmt> ::= <tdecl> | <fdecl> | <bstms>
  private void stmt() throws MyPLException {
    debug("<stmt>");
    if(currToken.type() == TokenType.TYPE || isDtype(currToken.type())){
      tdecl();
    }
    else if(currToken.type() == TokenType.FUN){
      fdecl();
    } else if (isBstmt(currToken.type())){
      bstmt();
    } else {
      error("Poor sstatement declaration");
    }
  }


  // <bstmts> ::= <bstmt> <bstmts> | epsilon
  private void bstmts() throws MyPLException {
    debug("<bstmts>");
    if(isBstmt(currToken.type())){
      bstmt();
      bstmts();
    }
  }
  
  //<bstmt ::= <vdecl> | <assign> | <cond> | <while> | <for> | <expr> | <exit>
  private void bstmt() throws MyPLException {
    debug("<bstmt>");
    if(currToken.type() == TokenType.VAR){
      vdecl();
    } else if (currToken.type() == TokenType.SET){
      assign();
    } else if (currToken.type() == TokenType.IF){
      cond();
    } else if (currToken.type() == TokenType.WHILE){
      while_stmt();
    } else if (currToken.type() == TokenType.FOR){
      for_stmt();
    } else if (isExpression(currToken.type())){
      expr();
    } else if (currToken.type() == TokenType.RETURN){
      exit();
    } else {
      error("Invalid bstmt");
    }
  }

  //<tdecl> ::= TYPE ID <vdecl> END
  private void tdecl() throws MyPLException {
    debug("<tdecl>");
    eat(TokenType.TYPE, "Expecting Type");
    eat(TokenType.ID, "Expecting ID");
    vdecls();
    eat(TokenType.END, "Expecting reserved word END");
  }

  //<fdecl> ::= FUN ( <dtype> | NIL ) ID LPAREN <params> RPAREN <bstmts> END
  private void fdecl() throws MyPLException {
    debug("<fdecl>");
    eat(TokenType.FUN, "Expecting function declaration");
    if(currToken.type() ==  TokenType.NIL){
      eat(TokenType.NIL, "Expecting Nil token");
    } else if (isDtype(currToken.type())) {
      dtype();
    } else {
      error("invalid function type");
    }
    eat(TokenType.ID, "Expecting ID");
    eat(TokenType.LPAREN, "Expecting Left Parens");
    params();
    eat(TokenType.RPAREN, "Expecting Right Parens");
    if (isBstmt(currToken.type())){
      bstmt();
    } else {
      error("Need a bstmt here");
    }
    eat(TokenType.END, "Expecting reserved word END");
  }

  //<vdecls> ::= <vdecl> <vdecls> | epsilon
  private void vdecls() throws MyPLException {
    debug("<vdecls>");
    vdecl();
    vdecls();
  }

  //<params> ::= <dtype> ID ( COMMA <dtype> ID ) | epsilon
  private void params() throws MyPLException {
    //checks to see if it will be a function without parameters
    debug("<params>");
    if (isDtype(currToken.type()) || currToken.type() == TokenType.ID){
      dtype();
      eat(TokenType.ID, "Expecting variable ID");
      //loop through to get every parameter variable
      while(currToken.type() == TokenType.COMMA){
        eat(TokenType.COMMA, "Expecting comma");
        if (isDtype(currToken.type()) || currToken.type() == TokenType.ID){
          dtype();
        } else {
          error("Need a data type or ID here");
        }
        eat(TokenType.ID, "expecting variable ID");
      }
    }
  }

  //<dtype> ::= INT_TYPE | DOUBLE_TYPE | BOOL_TYPE | CHAR_TYPE | STRING_TYPE | ID
  private void dtype() throws MyPLException {
    if(isDtype(currToken.type())){
      advance();
    } else if(currToken.type() == TokenType.ID){
      advance();
    } else {
      error("was expecting either an int, double, bool, char, string, or id");
    } 
  }

  //<exit> ::= RETURN ( <expr> | epsilon )
  private void exit() throws MyPLException {
    eat(TokenType.RETURN, "Expecting Return statement");
    if(isExpression(currToken.type())){
      expr();
    }
  }

  //<vdecl> ::= VAR ( <dtype> | epsilon ) ID ASSIGN <expr>
  //handling epsilon here with if statement
  private void vdecl() throws MyPLException {
    eat(TokenType.VAR, "Expecting toke VAR");
    if (isDtype(currToken.type())){
      dtype();
    } else if (currToken.type() == TokenType.ID){
      eat(TokenType.ID, "Expecting ID token");
      eat(TokenType.ASSIGN, "Expecting assignment token");
      if(isExpression(currToken.type())){
        expr();
      } else {
        error("Need an expression here");
      }
    } else {
      error("Either need a dval or ID token here");
    }
  }

  //<assign> ::= SET <lvalue> ASSIGN <expr>
  private void assign() throws MyPLException {
    eat(TokenType.SET, "Expecting SET token");
    if (currToken.type() == TokenType.ID){
      lvalue();
    } else {
      error("Expecting an lvalue here");
    }
    eat(TokenType.ASSIGN, "Expecting assignment token");
    if(isExpression(currToken.type())){
      expr();
    } else {
      error("Need an expression here");
    }
  }

  //<lvalue> ::= ID (DOT ID)*
  private void lvalue() throws MyPLException {
    eat(TokenType.ID, "Expecting ID token");
    if(currToken.type() == TokenType.DOT){
      while(currToken.type() == TokenType.DOT){
        eat(TokenType.DOT, "Expecting DOT token");
        eat(TokenType.ID, "Expecting ID token");
      }
    }
  }

  //<cond> ::= IF <expr> THEN <bstmts> <condt> END
  private void cond() throws MyPLException {
    eat(TokenType.IF, "Expecting IF token");
    if(isExpression(currToken.type())){
      expr();
    } else {
      error("Need an expression here");
    }
    eat(TokenType.THEN, "Expecting THEN token");
    if(isBstmt(currToken.type())){
      bstmt();
    } else {
      error("Need a bstmt here");
    }
    if(currToken.type() == TokenType.IF){
      condt();
    } else {
      error("Need a conditional tail here");
    }
    eat(TokenType.END, "Expecting END token");
  }

  //<condt> ::= ELIF <expr> THEN <bstmts> <condt> | ELSE <bstmts> | epsilon
  private void condt() throws MyPLException {
    if(currToken.type() == TokenType.ELIF){
      eat(TokenType.ELIF, "Expecting ELIF token");
      if(isExpression(currToken.type())){
        expr();
      } else {
        error("Need an expression here");
      }
      eat(TokenType.THEN, "Expecting THEN token");
      if(isBstmt(currToken.type())){
        bstmts();
      } else {
        error("Need an bstmt here");
      }
      if (currToken.type() == TokenType.IF){
        condt();
      } else {
        error("Need a conditional tail here");
      }
    }
    else if(currToken.type() == TokenType.ELSE){
      eat(TokenType.ELSE, "Expecting ELSE token");
      bstmts();
    }
  }

  //<while> ::= WHILE <expr> DO <bstmts> END
  private void while_stmt() throws MyPLException {
    eat(TokenType.WHILE, "Expecting WHILE token");
    if(isExpression(currToken.type())){
      expr();
    } else {
      error("Need an expression here");
    }
    eat(TokenType.DO, "Expecting DO token");
    if(isBstmt(currToken.type())){
      bstmts();
    } else {
      error("Need a bstmt here");
    }
    eat(TokenType.END, "Expecting END token");
  }

  //<for> ::= FOR ID ASSIGN <expr> TO <expr> DO <bstmts> END
  private void for_stmt() throws MyPLException {
    eat(TokenType.FOR, "Expecting FOR token");
    eat(TokenType.ID, "Expecting ID token");
    eat(TokenType.ASSIGN, "Expecting ASSIGN token");
    if(isExpression(currToken.type())){
      expr();
    } else {
      error("Need an expression here");
    }
    eat(TokenType.TO, "Expecting TO token");
    if(isExpression(currToken.type())){
      expr();
    } else {
      error("Need an expression here");
    }
    eat(TokenType.DO, "Expecting DO token");
    if(isBstmt(currToken.type())){
      bstmts();
    } else {
      error("Need a bstmt here");
    }
    eat(TokenType.END, "Expecting END token");
  }

  //<expr> ::= ( <rvalue> | NOT <expr> | LPAREN <expr> RPAREN ) ( <operator> <expr> | epsilon )
  private void expr() throws MyPLException {
    if(isExpression(currToken.type())){
      if(currToken.type() == TokenType.LPAREN){
        advance();
        expr();
        eat(TokenType.RPAREN, "Expecting RPAREN token");
      } else if (currToken.type() == TokenType.NOT){
        advance();
        expr();
      } else if (isRval(currToken.type())){
        rvalue();
      } else {
        error("Invalid expression");
      }
      //now utilize helper function
      if(isOperator(currToken.type())){
        advance();
        expr();
      }
    }
  }

  //<rvalue> ::= <pval> | NIL | NEW ID | <idrval> | NEG <expr>
  private void rvalue() throws MyPLException {
    if(currToken.type() == TokenType.NIL){
      advance();
    }
    else if(currToken.type() == TokenType.NEW){
      advance();
      eat(TokenType.ID, "Expecting ID token");
    }
    else if(currToken.type() == TokenType.NEG){
      advance();
      expr();
    }
    else if(isPval(currToken.type())){
      advance();
    } else if (currToken.type() == TokenType.ID){
      idrval();
    } else {
      error("Invalid right hand side value");
    }
  }

  //<idrval> ::= ID ( DOT ID )* | ID LPAREN <exprlist> RPAREN
  private void idrval() throws MyPLException {
    eat(TokenType.ID, "Expecting ID token");
    if(currToken.type() == TokenType.DOT){
      while(currToken.type() == TokenType.DOT){
        advance();
        eat(TokenType.ID, "Expecting ID token");
      }
    } else if (currToken.type() == TokenType.LPAREN) {
      eat(TokenType.LPAREN, "Expecting LPAREN token");
      exprlist();
      eat(TokenType.RPAREN, "Expecting RPAREN token");
    } else {
      error("Invalid right hand side ID");
    }
  }

  //<exprlist> ::= <expr> ( COMMA <expr> )* | epsilon
  private void exprlist() throws MyPLException {
    if(isExpression(currToken.type())){
      expr();
      while(currToken.type() == TokenType.COMMA){
        advance();
        expr();
      }
    }
  }
}