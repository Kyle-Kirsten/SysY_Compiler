package grammar;

import exceptions.*;
import wordTokenizer.Category;
import wordTokenizer.Tokenizer;
import wordTokenizer.Word;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 在读取的过程中打印文法信息
 * peek语法成分时把isOutput置错避免输出
 */
public class Grammarizer {
    private Tokenizer tokenizer;
    private BufferedWriter output;
    private boolean isOutput;

    public Grammarizer(Tokenizer tokenizer, BufferedWriter output) {
        this.isOutput = true;
        this.tokenizer = tokenizer;
        this.output = output;
    }

    private void write(String str) {
        if (isOutput) {
            try {
                output.write(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public CompUnit getAST() throws Exception {
        return readCompUnit();
    }

    /**
     * 读完后指针指向'}'
     * @return
     * @throws Exception
     */
    private CompUnit readCompUnit() throws Exception {
        List<TypeTree> decls = new ArrayList<>();
        List<FuncDecl> funcs = new ArrayList<>();
        //先匹配<Decl>
        do {
            tokenizer.peekStart();
            Word word1 = tokenizer.next();
            // 为常数
            if (word1.getCategory() == Category.CONSTTK) {
                tokenizer.peekEnd();
                decls.add(readDecl());
                continue;
            }
            // 类型不是int
            if (word1.getCategory() != Category.INTTK) {
                tokenizer.peekEnd();
                break;
            }
            // int后不是ident
            word1 = tokenizer.next();
            if (word1.getCategory() != Category.IDENFR) {
                tokenizer.peekEnd();
                break;
            }
            // ident后是'('
            word1 = tokenizer.next();
            if (word1.getCategory() == Category.LPARENT) {
                tokenizer.peekEnd();
                break;
            }
            // 是正常的Decl
            tokenizer.peekEnd();
            decls.add(readDecl());
        } while (true);
        //再匹配FuncDef
        do {
            tokenizer.peekStart();
            Word word1 = tokenizer.next();
            word1 = tokenizer.next();
            //类型后为main
            if (word1.getCategory() == Category.MAINTK) {
                tokenizer.peekEnd();
                break;
            }
            //正常函数
            tokenizer.peekEnd();
            funcs.add(readFuncDef());
        } while (true);
        //最后匹配MainFuncDef并打印输出
        funcs.add(readMainFuncDef());
        write(NonTerminal.CompUnit.format());

        return new CompUnit(decls, funcs);
    }

    /**
     * 读完后指向';', 不输出
     * @return
     * @throws Exception
     */
    private TypeTree readDecl() throws Exception {
        tokenizer.peekStart();
        Word word = tokenizer.next();
        if (word.getCategory() == Category.CONSTTK) {
            tokenizer.peekEnd();
            return readConstDecl();
        } else {
            tokenizer.peekEnd();
            return readVarDecl();
        }
    }

    /**
     * 读完后指向';'
     * @return
     * @throws Exception
     */
    private TypeTree readConstDecl() throws Exception {
        Word type;
        List<VarDecl> vars = new ArrayList<>();
        Word word = tokenizer.next();
        write(word.toString());
        if (word.getCategory() != Category.CONSTTK) {
            throw new ConstException("no const");
        }
        type = readBType();
        vars.add(readConstDef());
        word = tokenizer.next();
        write(word.toString());
        while (word.getCategory() == Category.COMMA) {
            vars.add(readConstDef());
            word = tokenizer.next();
            write(word.toString());
        }
        if (word.getCategory() != Category.SEMICN) {
            throw new ConstException("no semicolon");
        }
        write(NonTerminal.ConstDecl.format());
        return new TypeTree(type, vars, true);
    }

    /**
     * 读完后指向';'之前
     * @return
     * @throws Exception
     */
    private VarDecl readConstDef() throws Exception {
        Word ident;
        List<Node> arrayDims = new ArrayList<>();
        Node val;
        Word word = tokenizer.next();
        write(word.toString());
        if (word.getCategory() != Category.IDENFR) {
            throw new ConstException("no identifier");
        }
        ident = word;
        word = tokenizer.next();
        write(word.toString());
        while (word.getCategory() == Category.LBRACK) {
            arrayDims.add(readConstExp());
            word = tokenizer.next();
            write(word.toString());
            if (word.getCategory() != Category.RBRACK) {
                throw new ConstException("no ] for match");
            }
            word = tokenizer.next();
            write(word.toString());
        }
        if (word.getCategory() != Category.ASSIGN) {
            throw new ConstException("no initial value");
        }
        val = readConstInitValue();
        write(NonTerminal.ConstDef.format());
        return new VarDecl(new LVal(ident, arrayDims), val);
    }

    /**
     * 读完后指向','之前
     * @return
     * @throws Exception
     */
    private Node readConstInitValue() throws Exception {
        if (tokenizer.peek(1).get(0).getCategory() != Category.LBRACE) {
            Node node = readConstExp();
            write(NonTerminal.ConstInitVal.format());
            return node;
        } else {
            write(tokenizer.next().toString());
            List<Node> elems = new ArrayList<>();
            if (tokenizer.peek(1).get(0).getCategory() != Category.RBRACE) {
                elems.add(readConstInitValue());
                while (tokenizer.peek(1).get(0).getCategory() == Category.COMMA) {
                    write(tokenizer.next().toString());
                    elems.add(readConstInitValue());
                }
                if (tokenizer.peek(1).get(0).getCategory() != Category.RBRACE) {
                    throw new ConstException("no right brace for match");
                }
            }

            write(tokenizer.next().toString());
            write(NonTerminal.ConstInitVal.format());
            return new ArrayExp(elems);
        }
    }

    /**
     * 读完后指向']'前
     * @return
     * @throws Exception
     */
    private Node readConstExp() throws Exception {
        Node node = readAddExp();
        write(NonTerminal.ConstExp.format());
        return node;
    }


    /**
     * 读完后指向'int'，不输出
     * @return
     * @throws Exception
     */
    private Word readBType() throws Exception {
        Word word = tokenizer.next();
        if (word.getCategory() != Category.INTTK) {
            throw new TypeException("Wrong basic type");
        }
        write(word.toString());
        return word;
    }

    /**
     * 读完后指向';'
     * @return
     * @throws Exception
     */
    private TypeTree readVarDecl() throws Exception {
        Word type = readBType();
        List<VarDecl> vars = new ArrayList<>();
        vars.add(readVarDef());
        Word word;
        while ((word = tokenizer.peek(1).get(0)).getCategory() == Category.COMMA) {
            write(tokenizer.next().toString());
            vars.add(readVarDef());
        }
        if (word.getCategory() != Category.SEMICN) {
            throw new VarException("No semicolon");
        }
        write(tokenizer.next().toString());
        write(NonTerminal.VarDecl.format());
        return new TypeTree(type, vars, false);
    }

    /**
     * 读完后指向','前
     * @return
     * @throws Exception
     */
    private VarDecl readVarDef() throws Exception {
        Word ident = tokenizer.next();
        if (ident.getCategory() != Category.IDENFR) {
            throw new VarException("No identifier");
        }
        write(ident.toString());
        List<Node> arrayDims = new ArrayList<>();
        Word word;
        while ((word = tokenizer.peek(1).get(0)).getCategory() == Category.LBRACK) {
            write(tokenizer.next().toString());
            arrayDims.add(readConstExp());
            word = tokenizer.next();
            if (word.getCategory() != Category.RBRACK) {
                throw new VarException("No ] for match");
            }
            write(word.toString());
        }
        Node val;
        if (word.getCategory() == Category.ASSIGN) {
            write(tokenizer.next().toString());
            val = readInitVal();
        } else {
            val = Node.unDefined;
        }
        write(NonTerminal.VarDef.format());
        return new VarDecl(new LVal(ident, arrayDims), val);
    }

    /**
     * 读完时停在分号前
     * @return
     * @throws Exception
     */
    private Node readInitVal() throws Exception {
        if (tokenizer.peek(1).get(0).getCategory() != Category.LBRACE) {
            Node node = readExp();
            write(NonTerminal.InitVal.format());
            return node;
        }
        write(tokenizer.next().toString());
        List<Node> elems = new ArrayList<>();
        Word word = tokenizer.peek(1).get(0);
        if (word.getCategory() != Category.RBRACE) {
            elems.add(readInitVal());
            while ((word = tokenizer.peek(1).get(0)).getCategory() == Category.COMMA) {
                write(tokenizer.next().toString());
                elems.add(readInitVal());
            }
            if (word.getCategory() != Category.RBRACE) {
                throw new VarException("No matching }");
            }
        }
        write(tokenizer.next().toString());
        write(NonTerminal.InitVal.format());
        return new ArrayExp(elems);
    }

    /**
     * 读完后指向分号前
     * @return
     * @throws Exception
     */
    private Node readExp() throws Exception {
        Node node = readAddExp();
        write(NonTerminal.Exp.format());
        return node;
    }

    /**
     * 读完后指向'}'
     * @return
     */
    private FuncDecl readFuncDef() throws Exception {
        Word type = readFuncType();
        Word ident = tokenizer.next();
        if (ident.getCategory() != Category.IDENFR) {
            throw new FuncException("No identifier");
        }
        write(ident.toString());
        Word word = tokenizer.next();
        if (word != Word.LPARENT) {
            throw new FuncException("No (");
        }
        write(word.toString());
        List<FuncFParam> params = new ArrayList<>();
        if (tokenizer.peek(1).get(0) != Word.RPARENT) {
            params = readFuncFParams();
        }
        word = tokenizer.next();
        if (word != Word.RPARENT) {
            throw new FuncException("No )");
        }
        write(word.toString());
        Block block = readBlock();
        write(NonTerminal.FuncDef.format());
        return new FuncDecl(type, ident, params, block);
    }

    private Word readFuncType() throws Exception {
        Word type = tokenizer.next();
        if (type != Word.INT && type != Word.VOID) {
            throw new TypeException("Wrong func type");
        }
        write(type.toString());
        write(NonTerminal.FuncType.format());
        return type;
    }

    private List<FuncFParam> readFuncFParams() throws Exception {
        List<FuncFParam> params = new ArrayList<>();
        params.add(readFuncFParam());
        while (tokenizer.peek(1).get(0) == Word.COMMA) {
            write(tokenizer.next().toString());
            params.add(readFuncFParam());
        }
        write(NonTerminal.FuncFParams.format());
        return params;
    }

    private FuncFParam readFuncFParam() throws Exception {
        Word type = readBType();
        Word ident = tokenizer.next();
        if (ident.getCategory() != Category.IDENFR) {
            throw new FuncException("Function's parameter has no identifier");
        }
        write(ident.toString());
        List<Node> arrayDims = new ArrayList<>();
        Word word = tokenizer.peek(1).get(0);
        if (word == Word.LBRK) {
            write(tokenizer.next().toString());
            word = tokenizer.next();
            if (word != Word.RBRK) {
                throw new FuncException("Wrong function's array parameter");
            }
            write(word.toString());
            arrayDims.add(Node.unDefined);
            while (tokenizer.peek(1).get(0) == Word.LBRK) {
                write(tokenizer.next().toString());
                arrayDims.add(readConstExp());
                word = tokenizer.next();
                if (word != Word.RBRK) {
                    throw new FuncException("No matching ]");
                }
                write(word.toString());
            }
        }
        write(NonTerminal.FuncFParam.format());
        return new FuncFParam(type, ident, arrayDims);
    }

    /**
     * 读完后指向'}'
     * @return
     */
    private FuncDecl readMainFuncDef() throws Exception {
        Word word = tokenizer.next();
        if (word != Word.INT) {
            throw new FuncException("Wrong type of main function");
        }
        write(word.toString());
        word = tokenizer.next();
        if (word != Word.MAIN) {
            throw new FuncException("No Main function");
        }
        write(word.toString());
        word = tokenizer.next();
        if (word != Word.LPARENT) {
            throw new FuncException("No (");
        }
        write(word.toString());
        word = tokenizer.next();
        if (word != Word.RPARENT) {
            throw new FuncException("Exist parameters in main function");
        }
        write(word.toString());
        Block block = readBlock();
        write(NonTerminal.MainFuncDef.format());
        return new FuncDecl(Word.INT, Word.MAIN, new ArrayList<>(), block);
    }

    private Block readBlock() throws Exception {
        Word word = tokenizer.next();
        if (word != Word.LBIG) {
            throw new FuncException("No {");
        }
        write(word.toString());
        List<Node> stmts = new ArrayList<>();
        while (tokenizer.peek(1).get(0) != Word.RBIG) {
            stmts.add(readBlockItem());
        }
        write(tokenizer.next().toString());
        write(NonTerminal.Block.format());
        return new Block(stmts);
    }

    private Node readBlockItem() throws Exception {
        Word word = tokenizer.peek(1).get(0);
        if (word == Word.CONST || word == Word.INT) {
            return readDecl();
        }
        return readStmt();
    }

    private Node readStmt() throws Exception {
        Node stmt;
        Word word = tokenizer.peek(1).get(0);
        switch (word.getCategory()) {
            case IDENFR:
                tokenizer.peekStart();
                if (isOutput) {
                    isOutput = false;
                    readLVal();
                    word = tokenizer.next();
                    tokenizer.peekEnd();
                    isOutput = true;
                } else {
                    readLVal();
                    word = tokenizer.next();
                    tokenizer.peekEnd();
                }
                if (word == Word.ASSIGN) {
                    stmt = readAssign();
                } else {
                    stmt = readExp();
                }
                readSemiColon();
                break;
            case SEMICN:
                stmt = Node.unDefined;
                write(tokenizer.next().toString());
                break;
            case LBRACE:
                stmt = readBlock();
                break;
            case IFTK:
                stmt = readIf();
                break;
            case WHILETK:
                stmt = readWhile();
                break;
            case BREAKTK:
                write(tokenizer.next().toString());
                stmt = new BreakExp();
                readSemiColon();
                break;
            case CONTINUETK:
                write(tokenizer.next().toString());
                stmt = new ContinueExp();
                readSemiColon();
                break;
            case RETURNTK:
                stmt = readReturn();
                readSemiColon();
                break;
            case PRINTFTK:
                stmt = readPrint();
                readSemiColon();
                break;
            default:
                stmt = readExp();
                readSemiColon();
                break;
        }
        write(NonTerminal.Stmt.format());
        return stmt;
    }

    private void readSemiColon() throws Exception {
        Word word = tokenizer.next();
        if (word != Word.SEMICN) {
            throw new FuncException("Stmt has no semicolon");
        }
        write(word.toString());
    }

    private VarDecl readAssign() throws Exception {
        LVal lVal = readLVal();
        Node val;
        Word word = tokenizer.next();
        if (word != Word.ASSIGN) {
            throw new FuncException("No =");
        }
        write(word.toString());
        word = tokenizer.peek(1).get(0);
        if (word == Word.GETINT) {
            write(tokenizer.next().toString());
            word = tokenizer.next();
            if (word != Word.LPARENT) {
                throw new FuncException("No ( follows getint");
            }
            write(word.toString());
            word = tokenizer.next();
            if (word != Word.RPARENT) {
                throw new FuncException("No ) follows getint");
            }
            write(word.toString());
            val = new GetIntExp();
        } else {
            val = readExp();
        }
        return new VarDecl(lVal, val);
    }

    private LVal readLVal() throws Exception {
        Word ident = tokenizer.next();
        if (ident.getCategory() != Category.IDENFR) {
            throw new FuncException("No identifier in left value");
        }
        write(ident.toString());
        List<Node> index = new ArrayList<>();
        Word word;
        while (tokenizer.peek(1).get(0) == Word.LBRK) {
            write(tokenizer.next().toString());
            index.add(readExp());
            word = tokenizer.next();
            if (word != Word.RBRK) {
                throw new FuncException("No ]");
            }
            write(word.toString());
        }
        write(NonTerminal.LVal.format());
        return new LVal(ident, index);
    }

    private IfExp readIf() throws Exception {
        Word word = tokenizer.next();
        if (word != Word.IF) {
            throw new FuncException("No if");
        }
        write(word.toString());
        word = tokenizer.next();
        if (word != Word.LPARENT) {
            throw new FuncException("No ( matched for condition");
        }
        write(word.toString());
        Node cond = readCond();
        word = tokenizer.next();
        if (word != Word.RPARENT) {
            throw new FuncException("No ) matched for condition");
        }
        write(word.toString());
        Node ifBody = readStmt();
        Node elseBody = Node.unDefined;
        word = tokenizer.peek(1).get(0);
        if (word == Word.ELSE) {
            write(tokenizer.next().toString());
            elseBody = readStmt();
        }
        return new IfExp(cond, ifBody, elseBody);
    }

    private WhileExp readWhile() throws Exception {
        Word word = tokenizer.next();
        if (word != Word.WHILE) {
            throw new FuncException("No while");
        }
        write(word.toString());
        word = tokenizer.next();
        if (word != Word.LPARENT) {
            throw new FuncException("No ( matched for condition");
        }
        write(word.toString());
        Node cond = readCond();
        word = tokenizer.next();
        if (word != Word.RPARENT) {
            throw new FuncException("No ) matched for condition");
        }
        write(word.toString());
        Node whileBody = readStmt();
        return new WhileExp(cond, whileBody);
    }

    private ReturnExp readReturn() throws Exception {
        Word word = tokenizer.next();
        if (word != Word.RETURN) {
            throw new FuncException("No return");
        }
        write(word.toString());
        Node val = Node.unDefined;
        word = tokenizer.peek(1).get(0);
        if (word != Word.SEMICN) {
            val = readExp();
        }
        return new ReturnExp(val);

    }

    private PrintExp readPrint() throws Exception {
        Word word = tokenizer.next();
        if (word != Word.PRINTF) {
            throw new FuncException("No printf");
        }
        write(word.toString());
        word = tokenizer.next();
        if (word != Word.LPARENT) {
            throw new FuncException("No ( for printf");
        }
        write(word.toString());
        Word fStr = tokenizer.next();
        if (fStr.getCategory() != Category.STRCON) {
            throw new FuncException("No string for printf");
        }
        write(fStr.toString());
        List<Node> params = new ArrayList<>();
        while ((word = tokenizer.peek(1).get(0)) == Word.COMMA) {
            write(tokenizer.next().toString());
            params.add(readExp());
        }
        if (word != Word.RPARENT) {
            throw new FuncException("No ) for printf");
        }
        write(tokenizer.next().toString());
        return new PrintExp(fStr, params);
    }

    private Node readMulExp() throws Exception {
        Node val = readUnaryExp();
        Word op = tokenizer.peek(1).get(0);
        write(NonTerminal.MulExp.format());
        switch (op.getCategory()) {
            case MULT:
                write(tokenizer.next().toString());
                return new MulExp(val, readMulExp());
            case DIV:
                write(tokenizer.next().toString());
                return new DivExp(val, readMulExp());
            case MOD:
                write(tokenizer.next().toString());
                return new ModExp(val, readMulExp());
            default:
                return val;
        }
    }

    private Node readAddExp() throws Exception {
        Node val = readMulExp();
        Word op = tokenizer.peek(1).get(0);
        write(NonTerminal.AddExp.format());
        switch (op.getCategory()) {
            case PLUS:
                write(tokenizer.next().toString());
                return new AddExp(val, readAddExp());
            case MINU:
                write(tokenizer.next().toString());
                return new SubExp(val, readAddExp());
            default:
                return val;
        }
    }

    private Node readRelExp() throws Exception {
        Node val = readAddExp();
        Word op = tokenizer.peek(1).get(0);
        write(NonTerminal.RelExp.format());
        switch (op.getCategory()) {
            case LSS:
                write(tokenizer.next().toString());
                return new LessExp(val, readRelExp());
            case GRE:
                write(tokenizer.next().toString());
                return new GrtExp(val, readRelExp());
            case LEQ:
                write(tokenizer.next().toString());
                return new LeqExp(val, readRelExp());
            case GEQ:
                write(tokenizer.next().toString());
                return new GeqExp(val, readRelExp());
            default:
                return val;
        }
    }

    private Node readEqExp() throws Exception {
        Node val = readRelExp();
        Word op = tokenizer.peek(1).get(0);
        write(NonTerminal.EqExp.format());
        switch (op.getCategory()) {
            case EQL:
                write(tokenizer.next().toString());
                return new EqExp(val, readEqExp());
            case NEQ:
                write(tokenizer.next().toString());
                return new NeqExp(val, readEqExp());
            default:
                return val;
        }
    }

    private Node readLAndExp() throws Exception {
        Node val = readEqExp();
        Word op = tokenizer.peek(1).get(0);
        write(NonTerminal.LAndExp.format());
        switch (op.getCategory()) {
            case AND:
                write(tokenizer.next().toString());
                return new LAndExp(val, readLAndExp());
            default:
                return val;
        }
    }

    private Node readLOrExp() throws Exception {
        Node val = readLAndExp();
        Word op = tokenizer.peek(1).get(0);
        write(NonTerminal.LOrExp.format());
        switch (op.getCategory()) {
            case OR:
                write(tokenizer.next().toString());
                return new LOrExp(val, readLOrExp());
            default:
                return val;
        }
    }

    private Node readCond() throws Exception {
        Node node = readLOrExp();
        write(NonTerminal.Cond.format());
        return node;
    }

    private Node readPrimaryExp() throws Exception {
        Word word = tokenizer.peek(1).get(0);
        switch (word.getCategory()) {
            case LPARENT:
                write(tokenizer.next().toString());
                Node exp = readExp();
                word = tokenizer.next();
                if (word != Word.RPARENT) {
                    throw new FuncException("No ) matched for expression");
                }
                write(word.toString());
                write(NonTerminal.PrimaryExp.format());
                return new ParentExp(exp);
            case IDENFR:
                LVal lVal = readLVal();
                write(NonTerminal.PrimaryExp.format());
                return lVal;
            case INTCON:
                Number num = readNumber();
                write(NonTerminal.PrimaryExp.format());
                return num;
            default:
                throw new FuncException("Wrong match with Primary Expression");
        }
    }

    private Number readNumber() throws Exception {
        Word num = tokenizer.next();
        if (num.getCategory() != Category.INTCON) {
            throw new NumberException("Wrong number type");
        }
        write(num.toString());
        write(NonTerminal.Number.format());
        return new Number(num);
    }

    private Node readUnaryExp() throws Exception {
        Node res;
        List<Word> words = tokenizer.peek(3);
        switch (words.get(0).getCategory()) {
            case IDENFR:
                if (words.get(1) != Word.LPARENT) {
                    res = readPrimaryExp();
                    break;
                }
                write(tokenizer.next().toString());
                write(tokenizer.next().toString());
                List<Node> params = new ArrayList<>();
                if (words.get(2) == Word.RPARENT) {
                    write(tokenizer.next().toString());
                    res = new FuncCall(words.get(0), params);
                    break;
                }
                params = readFuncRParams();
                Word word = tokenizer.next();
                if (word != Word.RPARENT) {
                    throw new FuncException("No match ) for Function call");
                }
                write(word.toString());
                res = new FuncCall(words.get(0), params);
                break;
            case LPARENT:
            case INTCON:
                res = readPrimaryExp();
                break;
            case PLUS:
                write(tokenizer.next().toString());
                write(NonTerminal.UnaryOp.format());
                res = new PlusExp(readUnaryExp());
                break;
            case MINU:
                write(tokenizer.next().toString());
                write(NonTerminal.UnaryOp.format());
                res = new MinusExp(readUnaryExp());
                break;
            case NOT:
                write(tokenizer.next().toString());
                write(NonTerminal.UnaryOp.format());
                res = new NotExp(readUnaryExp());
                break;
            default:
                throw new ExpException("Wrong match for UnaryExpression");
        }
        write(NonTerminal.UnaryExp.format());
        return res;
    }


    private List<Node> readFuncRParams() throws Exception {
        List<Node> params = new ArrayList<>();
        params.add(readExp());
        while (tokenizer.peek(1).get(0) == Word.COMMA) {
            write(tokenizer.next().toString());
            params.add(readExp());
        }
        write(NonTerminal.FuncRParams.format());
        return params;
    }


}
