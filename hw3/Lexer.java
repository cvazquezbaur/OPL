/**
 * Author: Carlos Andres Vazquez Baur
 * Assign: 2
 *
 * The lexer implementation tokenizes a given input stream. The lexer
 * implements a pull-based model via the nextToken function such that
 * each call to nextToken advances the lexer to the next token (which
 * is returned by nextToken). The file has been completed read when
 * nextToken returns the EOS token. Lexical errors in the source file
 * result in the nextToken function throwing a MyPL Exception.
 */

import java.io.*;


public class Lexer {

  private BufferedReader buffer; // handle to input stream
  private int line;
  private int column;
  
  
  /** 
   */
  public Lexer(InputStream instream) {
    buffer = new BufferedReader(new InputStreamReader(instream));
    this.line = 1;
    this.column = 0;
  }

  
  /**
   * Returns next character in the stream. Returns -1 if end of file.
   */
  private int read() throws MyPLException {
    try {
      int ch = buffer.read();
      return ch;
    } catch(IOException e) {
      error("read error", line, column + 1);
    }
    return -1;
  }

  
  /** 
   * Returns next character without removing it from the stream.
   */
  private int peek() throws MyPLException {
    int ch = -1;
    try {
      buffer.mark(1);
      ch = read();
      buffer.reset();
    } catch(IOException e) {
      error("read error", line, column + 1);
    }
    return ch;
  }


  /**
   * Print an error message and exit the program.
   */
  private void error(String msg, int line, int column) throws MyPLException {
    throw new MyPLException("Lexer", msg, line, column);
  }
  /**
   */
  public Token nextToken() throws MyPLException {
    // TODO: your job in HW2 is to implement the next token function
    //column++;
    int initialColumn = column;
    String lexeme = "";
    int cur;
    String message;
    boolean isID = false;
    boolean isReserved = false;
    boolean isNum = false;
    cur = peek();
    
    //thinking of making while loop not include new line or whitespace so that it'll only loop
    //if it doesn't hit that, probably going to add that once I finish everything else.
    while(peek() != -1){
      //checks for new line character
      if((char)cur == '\n'){
        read();
        line += 1;
        column = 1;
        initialColumn = column;
        cur = peek();
      }

      //Going to take care of white space first
      else if(Character.isWhitespace(cur)) {
        read();
        column++;
        initialColumn = column;
        cur = peek();
      }

      //checking if it is a letter and will proceed to make a string and check
      //if it is a reserved word
      else if (Character.isLetter(cur)){
        if(!(isNum)){
          isID =  true;
          isReserved = true;
        }
        
        lexeme += (char)read();
        column++;
        cur = peek();
        while(Character.isLetter(cur)){
          lexeme += (char)read();
          column++;
          cur = peek();
        }

        //will uncheck if it is not a reserved word
        if(Character.isDigit(cur)){
          isReserved = false;
        }

        else if((char)cur == '_'){
          lexeme += read();
          column++;
          cur = peek();
          isReserved = false;
        }

        //will now go through all of the reserved words
        if(isReserved){
          if(lexeme.equals("int")){
            return new Token(TokenType.INT_TYPE, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("bool")){
            return new Token(TokenType.BOOL_TYPE, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("double")){
            return new Token(TokenType.DOUBLE_TYPE, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("char")){
            return new Token(TokenType.CHAR_TYPE, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("string")){
            return new Token(TokenType.STRING_TYPE, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("nil")){
            return new Token(TokenType.NIL, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("type")){
            return new Token(TokenType.TYPE, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("and")){
            return new Token(TokenType.AND, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("or")){
            return new Token(TokenType.OR, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("not")){
            return new Token(TokenType.NOT, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("neg")){
            return new Token(TokenType.NEG, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("while")){
            return new Token(TokenType.WHILE, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("for")){
            return new Token(TokenType.FOR, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("to")){
            return new Token(TokenType.TO, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("do")){
            return new Token(TokenType.DO, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("if")){
            return new Token(TokenType.IF, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("then")){
            return new Token(TokenType.THEN, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("elif")){
            return new Token(TokenType.ELIF, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("else")){
            return new Token(TokenType.ELSE, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("end")){
            return new Token(TokenType.END, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("fun")){
            return new Token(TokenType.FUN, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("var")){
            return new Token(TokenType.VAR, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("set")){
            return new Token(TokenType.SET, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("return")){
            return new Token(TokenType.RETURN, lexeme, line, initialColumn);
          }

          else if(lexeme.equals("new")){
            return new Token(TokenType.NEW, lexeme, line, initialColumn);
          }
        }

        if(!(Character.isDigit(cur)) && !(Character.isLetter(cur)) && (char)cur != '_'){
          return new Token(TokenType.ID, lexeme, line, initialColumn);
        }
      }

      //checks to see if it is a digit and will then create a string of digits
      //that may include decimals.
      else if (Character.isDigit(cur)){
        if(isID ==  false && isReserved == false){
          isNum = true;
        }

        lexeme += (char)read();
        column++;
        cur = peek();
        if(isNum){
        //going to check for invalid numbers
          if((char)cur == '0'){
            if(Character.isDigit(cur)){
              message = "Invalid int or double assignment";
              error(message, line, initialColumn);
            }
          }

          while(Character.isDigit(cur)){
            lexeme += (char)read();
            column++;
            cur = peek();
          }
          //deals with double values
          if((char)cur == '.'){
            lexeme += (char)read();
            column++;
            cur = peek();
            while(Character.isDigit(cur)){
              lexeme += (char)read();
              cur = peek();
              column++;
            }
            if((char)cur == '.'){
              message = "invalid double value";
              error(message, line, initialColumn);
            }
            return new Token(TokenType.DOUBLE_VAL, lexeme, line, initialColumn);
          }

          else if(Character.isLetter(cur)){
            message = "Cannot have letters in a number";
            error(message, line, initialColumn);
          }

          return new Token(TokenType.INT_VAL, lexeme, line, initialColumn);
        }

        //this will handle if it is an identifier
        else {
          while(Character.isDigit(cur)){
            lexeme += (char)read();
            column++;
            cur = peek();
          }
          if((char)cur == '_'){
            lexeme += (char)read();
            column++;
            cur = peek();
          }
          else if(!(Character.isLetter(cur)) && !(Character.isDigit(cur)) && (char)cur != '_'){
            return new Token(TokenType.ID, lexeme, line, initialColumn);
          }
        }
      }


      //handles comments
      else if((char)cur == '#'){
        read();
        cur = peek();
        //will go through until it hits a new line
        while((char)cur != '\n'){
          read();
          cur = peek();
        }
        //when it hits a new line, then it will increment the line appropriately
        //and go to first column
        if((char)cur == '\n'){
          read();
          line++;
          column = 1;
          initialColumn = column;
          cur = peek();
        }
      }

      //now for symbols
      else if((char)cur == ':'){
        lexeme += (char)read();
        column++;
        cur = peek();
        if((char)cur == '='){
          lexeme += (char)read();
          column++;
          return new  Token(TokenType.ASSIGN, ":=", line, initialColumn);
        } else {
          message = "Ivalid assignment declaration";
          error(message, line, initialColumn);
        }
      }

      else if((char)cur == '.'){
        lexeme += (char)read();
        column++;
        return new Token(TokenType.DOT, lexeme, line, initialColumn);
      }

      else if((char)cur == ','){
        lexeme += (char)read();
        column++;
        return new Token(TokenType.COMMA, lexeme, line, initialColumn);
      }

      else if((char)cur == '+'){
        lexeme += (char)read();
        return new Token(TokenType.PLUS, lexeme, line, initialColumn);
      }

      else if((char)cur == '-'){
        lexeme += (char)read();
        column++;
        return new Token(TokenType.MINUS, lexeme, line, initialColumn);
      }

      else if((char)cur == '*'){
        read();
        return new Token(TokenType.MULTIPLY, "*", line, column);
      }

      else if((char)cur == '/'){
        read();
        return new Token(TokenType.DIVIDE, "/", line, column);
      }

      else if((char)cur == '%'){
        read();
        return new Token(TokenType.MODULO, "%", line, column);
      }

      else if((char)cur == '='){
        lexeme += (char)read();
        //add in error if they do a '=='
        cur = peek();
        if((char)cur == '='){
          message = "Invalid comparison with '=='";
          column++;
          error(message, line, initialColumn);
        } else {
          return new Token(TokenType.EQUAL, lexeme, line, column);
        }
      }

      else if((char)cur == '>'){
        lexeme += (char)read();
        column++;
        cur = peek();
        if((char)cur == '='){
          lexeme += (char)read();
          column++;
          return new Token(TokenType.GREATER_THAN_EQUAL, lexeme, line, initialColumn);
        } else {
          return new Token(TokenType.GREATER_THAN, lexeme, line, initialColumn);
        }
      }

      //deals with strings
      else if((char)cur == '\"'){
        read();
        column++;
        cur = peek();
        while((char)cur != '\"'){
          //check to see if there is a new line in the string
          if((char)cur == '\n'){
            message = "Cannot have new lines in a string";
            error(message, line, column);
          }
          lexeme += (char)read();
          column++;
          cur = peek();
        }
        column++;
        read();
        return new Token(TokenType.STRING_VAL, lexeme, line, initialColumn);
      }

      //deals with character values
      else if((char)cur == '\''){
        read();
        column++;
        cur = peek();
        while((char)cur != '\''){
          lexeme += (char)read();
          column++;
          cur = peek();
        }
        read();
        column++;
        return new Token(TokenType.CHAR_VAL, lexeme, line, initialColumn);
      }
      else if((char)cur == '<'){
        lexeme += (char)read();
        cur = peek();
        if((char)cur == '='){
          lexeme += (char)read();
          column++;
          return new Token(TokenType.LESS_THAN_EQUAL, lexeme, line, initialColumn);
        } else {
          return new Token(TokenType.LESS_THAN, lexeme, line, initialColumn);
        }
      }

      else if((char)cur == '!'){
        lexeme += (char)read();
        column++;
        cur = peek();
        if((char)cur == '='){
          lexeme += (char)read();
          column++;
          return new Token(TokenType.NOT_EQUAL, lexeme, line, initialColumn);
        } else {
          return new Token(TokenType.NOT, lexeme, line, initialColumn);
        }
      }

      else if((char)cur == '('){
        lexeme += (char)read();
        column++;
        return new Token(TokenType.LPAREN, lexeme, line, initialColumn);
      }

      else if((char)cur == ')'){
        lexeme += (char)read();
        column++;
        return new Token(TokenType.RPAREN, lexeme, line, initialColumn);
      }
    }
    
    if (peek() == -1){
      return new Token(TokenType.EOS, "", line, column);
    }
    return null;
  }
}