package grammar;

import exceptions.*;
import wordTokenizer.Category;
import wordTokenizer.FString;
import wordTokenizer.Tokenizer;
import wordTokenizer.Word;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
 * 在读取的过程中打印文法信息
 * peek语法成分时把isOutput置错避免输出
 * debug控制错误信息的输出
 */
public class Grammarizer {
    private Tokenizer tokenizer;
    private BufferedWriter output;
    private boolean isOutput;
    private boolean debug;
    private Stack<Map<String, Symbol>> symStack; // 变量符号表
    private Map<String, FuncSymbol> funcs; //函数符号表
    private int loopLevel; // 当前的循环层数
    private Category retType; // 当前函数的返回值类型

    public Grammarizer(Tokenizer tokenizer, BufferedWriter output) {
        this.isOutput = false;
        this.tokenizer = tokenizer;
        this.output = output;
        this.debug = true;
        this.symStack = new Stack<>();
        this.funcs = new HashMap<>();
        this.loopLevel = 0;
        this.retType = Category.VOIDTK;
    }

    public Grammarizer(Tokenizer tokenizer, BufferedWriter output, boolean grammarOut,
                       boolean debug) {
        this(tokenizer, output);
        this.isOutput = grammarOut;
        this.debug = debug;
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

    private void writeError(Exception e) {
        UserException userException;
        if (e instanceof UserException) {
            userException = (UserException) e;
            if (debug) {
                userException.prtError(output);
            }
        } else {
            e.printStackTrace();
        }
    }

    public CompUnit getAST() throws Exception {
        return readCompUnit();
    }

    /**
     * 读到一个合法的单词为止，不合法的情况直接输出错误码
     * @return 合法单词
     */
    private Word readWord() throws Exception {
        Word word;
        do {
            try {
                word = tokenizer.next();
            } catch (Exception e) {
                writeError(e);
                tokenizer.skipLine();
                continue;
            }
            break;
        } while (true);
        return word;
    }

    private List<Word> peekWord(int n) throws Exception {
        List<Word> words = new ArrayList<>();
        do {
            try {
                tokenizer.peekStart();
                for (int i = 0; i < n; i++) {
                    words.add(tokenizer.next());
                }
                tokenizer.peekEnd();
            } catch (Exception e) {
                words = new ArrayList<>();
                writeError(e);
                tokenizer.skipLine();
                tokenizer.peekTo();
                continue;
            }
            break;
        } while (true);
        return words;
    }

    /**
     * 读完后指针指向'}'
     * @return
     * @throws Exception
     */
    private CompUnit readCompUnit() throws Exception {
        List<TypeTree> decls = new ArrayList<>();
        List<FuncDecl> funcs = new ArrayList<>();
        Map<String, Symbol> realm = new HashMap<>();
        symStack.push(realm);
        //先匹配<Decl>
        do {
            tokenizer.peekStart();
            Word word1;
            word1 = tokenizer.next();
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
            Word word1;
            word1 = tokenizer.next();
            word1 = tokenizer.next();
            //类型后为main
            if (word1.getCategory() == Category.MAINTK) {
                tokenizer.peekEnd();
                break;
            }
            //读到结束符
            if (word1.getCategory() == Category.END) {
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
        Word word = tokenizer.peek(1).get(0);
        if (word.getCategory() == Category.CONSTTK) {
            return readConstDecl();
        } else {
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
        VarDecl varDecl;
        LVal lVal;
        Map<String, Symbol> realm = symStack.peek();
        Word word = tokenizer.next();
        write(word.toString());
        if (word.getCategory() != Category.CONSTTK) {
            throw new ConstException("no const");
        }
        type = readBType();
        varDecl = readConstDef();
        lVal = varDecl.getlVal();
        vars.add(varDecl);
        realm.put(lVal.getName(),
                new ConstSymbol(lVal.getName(), type.getCategory(), lVal.getIndexNum()));
        word = tokenizer.peek(1).get(0);
        while (word.getCategory() == Category.COMMA) {
            write(tokenizer.next().toString());
            varDecl = readConstDef();
            lVal = varDecl.getlVal();
            vars.add(varDecl);
            realm.put(lVal.getName(),
                    new ConstSymbol(lVal.getName(), type.getCategory(), lVal.getIndexNum()));
            word = tokenizer.peek(1).get(0);
        }
        if (word.getCategory() != Category.SEMICN) {
            writeError(new SemicolonLackException("No semicolon", tokenizer.getLineNo()));
        } else {
            write(tokenizer.next().toString());
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
        List<ValExp> arrayDims = new ArrayList<>();
        Node val;
        Map<String, Symbol> realm = symStack.peek();
        Word word = tokenizer.next();
        write(word.toString());
        if (word.getCategory() != Category.IDENFR) {
            throw new ConstException("no identifier");
        }
        ident = word;
        if (realm.containsKey(ident.getName())) {
            writeError(new DupNameException("Duplicated name", ident.getLineNo()));
        }
        word = tokenizer.next();
        write(word.toString());
        while (word.getCategory() == Category.LBRACK) {
            arrayDims.add((ValExp) readConstExp());
            word = tokenizer.peek(1).get(0);
            if (word.getCategory() != Category.RBRACK) {
                writeError(new RBracketException("No ]", tokenizer.getLineNo()));
            } else {
                write(tokenizer.next().toString());
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
        VarDecl varDecl;
        LVal lVal;
        Map<String, Symbol> realm = symStack.peek();
        varDecl = readVarDef();
        lVal = varDecl.getlVal();
        vars.add(varDecl);
        realm.put(lVal.getName(), new VarSymbol(lVal.getName(), type.getCategory(), lVal.getIndexNum()));
        Word word;
        while ((word = tokenizer.peek(1).get(0)).getCategory() == Category.COMMA) {
            write(tokenizer.next().toString());
            varDecl = readVarDef();
            lVal = varDecl.getlVal();
            vars.add(varDecl);
            realm.put(lVal.getName(),
                    new VarSymbol(lVal.getName(), type.getCategory(), lVal.getIndexNum()));
        }
        if (word.getCategory() != Category.SEMICN) {
            writeError(new SemicolonLackException("No ;", tokenizer.getLineNo()));
        } else {
            write(tokenizer.next().toString());
        }
        write(NonTerminal.VarDecl.format());
        return new TypeTree(type, vars, false);
    }

    /**
     * 读完后指向','前
     * @return
     * @throws Exception
     */
    private VarDecl readVarDef() throws Exception {
        Map<String, Symbol> realm = symStack.peek();
        Word ident = tokenizer.next();
        if (ident.getCategory() != Category.IDENFR) {
            throw new VarException("No identifier");
        }
        if (realm.containsKey(ident.getName())) {
            writeError(new DupNameException("dup name", ident.getLineNo()));
        }
        write(ident.toString());
        List<ValExp> arrayDims = new ArrayList<>();
        Word word;
        while ((word = tokenizer.peek(1).get(0)).getCategory() == Category.LBRACK) {
            write(tokenizer.next().toString());
            arrayDims.add((ValExp) readConstExp());
            word = tokenizer.peek(1).get(0);
            if (word.getCategory() != Category.RBRACK) {
                writeError(new RBracketException("No ]", tokenizer.getLineNo()));
            } else {
                write(tokenizer.next().toString());
            }
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
        Map<String, FuncSymbol> realm = funcs;
        Word type = readFuncType();
        Word ident = tokenizer.next();
        if (ident.getCategory() != Category.IDENFR) {
            throw new FuncException("No identifier");
        }
        if (realm.containsKey(ident.getName())) {
            writeError(new DupNameException("Dup func", ident.getLineNo()));
        }
        write(ident.toString());
        Word word = tokenizer.next();
        if (word.getCategory() != Category.LPARENT) {
            throw new FuncException("No (");
        }
        write(word.toString());
        List<FuncFParam> params = new ArrayList<>();
        symStack.push(new HashMap<>());
        if (tokenizer.peek(1).get(0).getCategory() != Category.RPARENT) {
            params = readFuncFParams();
        }
        List<VarSymbol> paramSymbols = new ArrayList<>();
        for (FuncFParam param : params) {
            paramSymbols.add(param.toVarSymbol());
        }
        realm.put(ident.getName(),
                new FuncSymbol(ident.getName(), type.getCategory(), paramSymbols));
        word = tokenizer.peek(1).get(0);
        if (word.getCategory() != Category.RPARENT) {
            writeError(new RParentException("No )", tokenizer.getLineNo()));
        } else {
            write(tokenizer.next().toString());
        }
        retType = type.getCategory();
        Block block = readBlock(true);
        retType = Category.VOIDTK;
        symStack.pop();
        if ( type.getCategory() != Category.VOIDTK && !(block.getLastStmt() instanceof ReturnExp) )
        {
            writeError(new ReturnLackException("No return statement", block.getEndLine()));
        }
        write(NonTerminal.FuncDef.format());
        return new FuncDecl(type, ident, params, block);
    }

    private Word readFuncType() throws Exception {
        Word type = tokenizer.next();
        if (type.getCategory() != Category.INTTK && type.getCategory() != Category.VOIDTK) {
            throw new TypeException("Wrong func type");
        }
        write(type.toString());
        write(NonTerminal.FuncType.format());
        return type;
    }

    private List<FuncFParam> readFuncFParams() throws Exception {
        List<FuncFParam> params = new ArrayList<>();
        params.add(readFuncFParam());
        while (tokenizer.peek(1).get(0).getCategory() == Category.COMMA) {
            write(tokenizer.next().toString());
            params.add(readFuncFParam());
        }
        write(NonTerminal.FuncFParams.format());
        return params;
    }

    private FuncFParam readFuncFParam() throws Exception {
        Map<String, Symbol> realm = symStack.peek();
        Word type = readBType();
        Word ident = tokenizer.next();
        if (ident.getCategory() != Category.IDENFR) {
            throw new FuncException("Function's parameter has no identifier");
        }
        if (realm.containsKey(ident.getName())) {
            writeError(new DupNameException("Dup parameter", ident.getLineNo()));
        }
        write(ident.toString());
        List<Node> arrayDims = new ArrayList<>();
        Word word = tokenizer.peek(1).get(0);
        if (word.getCategory() == Category.LBRACK) {
            write(tokenizer.next().toString());
            word = tokenizer.peek(1).get(0);
            if (word.getCategory() != Category.RBRACK) {
                writeError(new RBracketException("No ]", tokenizer.getLineNo()));
            } else {
                write(tokenizer.next().toString());
            }
            arrayDims.add(Node.unDefined);
            while (tokenizer.peek(1).get(0).getCategory() == Category.LBRACK) {
                write(tokenizer.next().toString());
                arrayDims.add(readConstExp());
                word = tokenizer.peek(1).get(0);
                if (word.getCategory() != Category.RBRACK) {
                    writeError(new RBracketException("No ]", tokenizer.getLineNo()));
                } else {
                    write(tokenizer.next().toString());
                }
            }
        }
        write(NonTerminal.FuncFParam.format());
        realm.put(ident.getName(),
                new VarSymbol(ident.getName(), type.getCategory(), arrayDims.size()));
        return new FuncFParam(type, ident, arrayDims);
    }

    /**
     * 读完后指向'}'
     * @return
     */
    private FuncDecl readMainFuncDef() throws Exception {
        Word word = tokenizer.next();
        if (word.getCategory() != Category.INTTK) {
            throw new FuncException("Wrong type of main function");
        }
        write(word.toString());
        word = tokenizer.next();
        if (word.getCategory() != Category.MAINTK) {
            throw new FuncException("No Main function");
        }
        write(word.toString());
        word = tokenizer.next();
        if (word.getCategory() != Category.LPARENT) {
            throw new FuncException("No (");
        }
        write(word.toString());
        word = tokenizer.next();
        if (word.getCategory() != Category.RPARENT) {
            throw new FuncException("Exist parameters in main function");
        }
        write(word.toString());
        retType = Category.INTTK;
        symStack.push(new HashMap<>());
        Block block = readBlock(true);
        symStack.pop();
        retType = Category.VOIDTK;
        if (!(block.getLastStmt() instanceof ReturnExp)) {
            writeError(new ReturnLackException("No return in main", block.getEndLine()));
        }
        write(NonTerminal.MainFuncDef.format());
        return new FuncDecl(Word.INT, Word.MAIN, new ArrayList<>(), block);
    }

    private Block readBlock() throws Exception {
        return readBlock(false);
    }

    private Block readBlock(boolean sameBlock) throws Exception {
        if (!sameBlock) {
            symStack.push(new HashMap<>());
        }
        Word word = tokenizer.next();
        if (word.getCategory() != Category.LBRACE) {
            throw new FuncException("No {");
        }
        write(word.toString());
        List<Node> stmts = new ArrayList<>();
        while (tokenizer.peek(1).get(0).getCategory() != Category.RBRACE) {
            stmts.add(readBlockItem());
        }
        word = tokenizer.next();
        write(word.toString());
        write(NonTerminal.Block.format());
        if (!sameBlock) {
            symStack.pop();
        }
        return new Block(stmts, word);
    }

    private Node readBlockItem() throws Exception {
        Word word = tokenizer.peek(1).get(0);
        if (word.getCategory() == Category.CONSTTK || word.getCategory() == Category.INTTK) {
            return readDecl();
        }
        return readStmt();
    }

    private Node readStmt() throws Exception {
        Node stmt;
        Word word = tokenizer.peek(1).get(0);
        switch (word.getCategory()) {
            case IDENFR:
                boolean isOutput0 = isOutput;
                boolean isDebug = debug;
                isOutput = false;
                debug = false;
                tokenizer.peekStart();
                readLVal();
                word = tokenizer.next();
                tokenizer.peekEnd();
                isOutput = isOutput0;
                debug = isDebug;

                if (word.getCategory() == Category.ASSIGN) {
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
                if (loopLevel <= 0) {
                    writeError(new BreakContinueException("Break in non-loop", word.getLineNo()));
                }
                write(tokenizer.next().toString());
                stmt = new BreakExp();
                readSemiColon();
                break;
            case CONTINUETK:
                if (loopLevel <= 0) {
                    writeError(new BreakContinueException("Continue in non-loop",
                            word.getLineNo()));
                }
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
        Word word = tokenizer.peek(1).get(0);
        if (word.getCategory() != Category.SEMICN) {
            writeError(new SemicolonLackException("No ;", tokenizer.getLineNo()));
        } else {
            write(tokenizer.next().toString());
        }
    }

    private VarDecl readAssign() throws Exception {
        LVal lVal = readLVal();
        if (lVal.getSymbol() instanceof ConstSymbol) {
            writeError(new ConstChangeException("Constant can't be assigned", lVal.getLineNo()));
        }
        Node val;
        Word word = tokenizer.next();
        if (word.getCategory() != Category.ASSIGN) {
            throw new FuncException("No =");
        }
        write(word.toString());
        word = tokenizer.peek(1).get(0);
        if (word.getCategory() == Category.GETINTTK) {
            write(tokenizer.next().toString());
            word = tokenizer.next();
            if (word.getCategory() != Category.LPARENT) {
                throw new FuncException("No ( follows getint");
            }
            write(word.toString());
            word = tokenizer.peek(1).get(0);
            if (word.getCategory() != Category.RPARENT) {
                writeError(new RParentException("No ) for getint", tokenizer.getLineNo()));
            } else {
                write(tokenizer.next().toString());
            }
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
        Symbol lValSymbol = Symbol.undefined;
        for (int i = symStack.size(); i > 0; i--) {
            Symbol tmp =  symStack.get(i - 1).get(ident.getName());
            if (tmp != null) {
                lValSymbol = tmp;
                break;
            }
        }
        if (lValSymbol == Symbol.undefined) {
            writeError(new UndeclaredException("Can't find lVal", ident.getLineNo()));
        }
        List<ValExp> index = new ArrayList<>();
        Word word;
        while (tokenizer.peek(1).get(0).getCategory() == Category.LBRACK) {
            write(tokenizer.next().toString());
            index.add((ValExp) readExp());
            word = tokenizer.peek(1).get(0);
            if (word.getCategory() != Category.RBRACK) {
                writeError(new RBracketException("No ]", tokenizer.getLineNo()));
            } else {
                write(tokenizer.next().toString());
            }
        }
        write(NonTerminal.LVal.format());
        return new LVal(ident, index, lValSymbol);
    }

    private IfExp readIf() throws Exception {
        Word word = tokenizer.next();
        if (word.getCategory() != Category.IFTK) {
            throw new FuncException("No if");
        }
        write(word.toString());
        word = tokenizer.next();
        if (word.getCategory() != Category.LPARENT) {
            throw new FuncException("No ( matched for condition");
        }
        write(word.toString());
        Node cond = readCond();
        word = tokenizer.peek(1).get(0);
        if (word.getCategory() != Category.RPARENT) {
            writeError(new RParentException("No ) for if", tokenizer.getLineNo()));
        } else {
            write(tokenizer.next().toString());
        }
        Node ifBody = readStmt();
        Node elseBody = Node.unDefined;
        word = tokenizer.peek(1).get(0);
        if (word.getCategory() == Category.ELSETK) {
            write(tokenizer.next().toString());
            elseBody = readStmt();
        }
        return new IfExp(cond, ifBody, elseBody);
    }

    private WhileExp readWhile() throws Exception {
        Word word = tokenizer.next();
        if (word.getCategory() != Category.WHILETK) {
            throw new FuncException("No while");
        }
        write(word.toString());
        word = tokenizer.next();
        if (word.getCategory() != Category.LPARENT) {
            throw new FuncException("No ( matched for condition");
        }
        write(word.toString());
        Node cond = readCond();
        word = tokenizer.peek(1).get(0);
        if (word.getCategory() != Category.RPARENT) {
            writeError(new RParentException("No ) for while", tokenizer.getLineNo()));
        } else {
            write(tokenizer.next().toString());
        }
        loopLevel++;
        Node whileBody = readStmt();
        loopLevel--;
        return new WhileExp(cond, whileBody);
    }

    private ReturnExp readReturn() throws Exception {
        Word word = tokenizer.next();
        if (word.getCategory() != Category.RETURNTK) {
            throw new FuncException("No return");
        }
        write(word.toString());
        Word ret = word;
        Node val = Node.unDefined;
        word = tokenizer.peek(1).get(0);
        if (word.getCategory() != Category.SEMICN) {
            val = readExp();
            if (retType == Category.VOIDTK) {
                writeError(new ReturnExistException("Wrong return", ret.getLineNo()));
            }
        }
        return new ReturnExp(val);

    }

    private PrintExp readPrint() throws Exception {
        Word word = tokenizer.next();
        if (word.getCategory() != Category.PRINTFTK) {
            throw new FuncException("No printf");
        }
        write(word.toString());
        Word printf = word;
        word = tokenizer.next();
        if (word.getCategory() != Category.LPARENT) {
            throw new FuncException("No ( for printf");
        }
        write(word.toString());
        word = tokenizer.next();
        if (word.getCategory() != Category.STRCON) {
            throw new FuncException("No string for printf");
        }
        FString fStr = (FString) word;
        if (fStr.isIllegal()) {
            writeError(new IllegalFStrCharException("Illegal character", fStr.getLineNo()));
        }
        write(fStr.toString());
        List<Node> params = new ArrayList<>();
        while ((word = tokenizer.peek(1).get(0)).getCategory() == Category.COMMA) {
            write(tokenizer.next().toString());
            params.add(readExp());
        }
        if (word.getCategory() != Category.RPARENT) {
            writeError(new RParentException("NO ) for printf", tokenizer.getLineNo()));
        } else {
            write(tokenizer.next().toString());
        }
        if (params.size() != fStr.getNum()) {
            writeError(new PrintfException("Unmatched number of parameters", printf.getLineNo()));
        }
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
                word = tokenizer.peek(1).get(0);
                if (word.getCategory() != Category.RPARENT) {
                    writeError(new RParentException("No ) for primary expression",
                            tokenizer.getLineNo()));
                } else {
                    write(tokenizer.next().toString());
                }
                write(NonTerminal.PrimaryExp.format());
                return new ParentExp((ValExp) exp);
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
                if (words.get(1).getCategory() != Category.LPARENT) {
                    res = readPrimaryExp();
                    break;
                }
                FuncSymbol funcSymbol = funcs.get(words.get(0).getName());
                if (funcSymbol == null) {
                    writeError(new UndeclaredException("No such function",
                            words.get(0).getLineNo()));
                }
                write(tokenizer.next().toString());
                write(tokenizer.next().toString());
                List<Node> params = new ArrayList<>();
                if (words.get(2).getCategory() == Category.RPARENT) {
                    write(tokenizer.next().toString());
                    if (funcSymbol != null && funcSymbol.getParams().size() != 0) {
                        writeError(new ParamNumException("Number of Parameter is not zero",
                                words.get(0).getLineNo()));
                    }
                    res = new FuncCall(words.get(0), params,
                            funcSymbol == null ? funcSymbol.getReturnType() : Category.VOIDTK);
                    break;
                }
                params = readFuncRParams();
                if (funcSymbol != null && funcSymbol.getParams().size() != params.size()) {
                    writeError(new ParamNumException("Unmatched number of parameters",
                            words.get(0).getLineNo()));
                } else if (funcSymbol != null) {
                    for (int i = 0; i < params.size(); i++) {
                        if (!(params.get(i) instanceof ValExp)) {
                            writeError(new ParamTypeException("Undefined type",
                                    words.get(0).getLineNo()));
                            break;
                        }
                        ValExp valExp = (ValExp) params.get(i);
                        VarSymbol varSymbol = funcSymbol.getParams().get(i);
                        if (valExp.getType() != varSymbol.getType()
                                || valExp.getDims() != varSymbol.getDims()) {
                            writeError(new ParamTypeException("Unmatched parameter",
                                    words.get(0).getLineNo()));
                            break;
                        }
                    }
                }
                Word word = tokenizer.peek(1).get(0);
                if (word.getCategory() != Category.RPARENT) {
                    writeError(new RParentException("No ) for function call",
                            tokenizer.getLineNo()));
                } else {
                    write(tokenizer.next().toString());
                }
                res = new FuncCall(words.get(0), params,
                        funcSymbol == null ? funcSymbol.getReturnType() : Category.VOIDTK);
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
        while (tokenizer.peek(1).get(0).getCategory() == Category.COMMA) {
            write(tokenizer.next().toString());
            params.add(readExp());
        }
        write(NonTerminal.FuncRParams.format());
        return params;
    }


}
