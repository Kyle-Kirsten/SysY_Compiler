package wordTokenizer;

import java.util.HashMap;
import java.util.Map;

public class Word {
    public static final Word END = new Word("", Category.END),
            ERROR = new Word("", Category.ERROR),
            ZERO = new Word("0", Category.INTCON),

            NOT = new Word("!", Category.NOT),
            AND = new Word("&&", Category.AND),
            OR = new Word("||", Category.OR),
            PLUS = new Word("+", Category.PLUS),
            MINUS = new Word("-", Category.MINU),
            MULT = new Word("*", Category.MULT),
            DIV = new Word("/", Category.DIV),
            MOD = new Word("%", Category.MOD),
            LST = new Word("<", Category.LSS),
            LEQ = new Word("<=", Category.LEQ),
            GRT = new Word(">", Category.GRE),
            GEQ = new Word(">=", Category.GEQ),
            EQL = new Word("==", Category.EQL),
            NEQ = new Word("!=", Category.NEQ),
            ASSIGN = new Word("=", Category.ASSIGN),
            SEMICN = new Word(";", Category.SEMICN),
            COMMA = new Word(",", Category.COMMA),
            LPARENT = new Word("(", Category.LPARENT),
            RPARENT = new Word(")", Category.RPARENT),
            LBRK = new Word("[", Category.LBRACK),
            RBRK = new Word("]", Category.RBRACK),
            LBIG = new Word("{", Category.LBRACE),
            RBIG = new Word("}", Category.RBRACE),

            MAIN = new Word("main", Category.MAINTK),
            CONST = new Word("const", Category.CONSTTK),
            INT = new Word("int", Category.INTTK),
            BREAK = new Word("break", Category.BREAKTK),
            CONTINUE = new Word("continue", Category.CONTINUETK),
            IF = new Word("if", Category.IFTK),
            ELSE = new Word("else", Category.ELSETK),
            WHILE = new Word("while", Category.WHILETK),
            GETINT = new Word("getint", Category.GETINTTK),
            PRINTF = new Word("printf", Category.PRINTFTK),
            RETURN = new Word("return", Category.RETURNTK),
            VOID = new Word("void", Category.VOIDTK);

    public static final Map<String, Word> KEY_WORD = new HashMap<String, Word>(){{
       put(MAIN.name, MAIN);
       put(CONST.name, CONST);
       put(INT.name, INT);
       put(BREAK.name, BREAK);
       put(CONTINUE.name, CONTINUE);
       put(IF.name, IF);
       put(ELSE.name, ELSE);
       put(WHILE.name, WHILE);
       put(GETINT.name, GETINT);
       put(PRINTF.name, PRINTF);
       put(RETURN.name, RETURN);
       put(VOID.name, VOID);
    }};

    private String name;
    private Category category;
    private int lineNo;

    public Word(String name, Category category) {
        this.name = name;
        this.category = category;
        this.lineNo = 0;
    }

    public Word(String name, Category category, int lineNo) {
        this(name, category);
        this.lineNo = lineNo;
    }

    @Override
    public String toString() {
        return category.toString() + " " + name + "\n";
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public int getLineNo() {
        return lineNo;
    }
}
