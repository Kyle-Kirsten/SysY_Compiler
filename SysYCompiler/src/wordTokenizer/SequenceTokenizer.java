package wordTokenizer;

import exceptions.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


class State {
    private char sym;
    private int lineNo;
    private long pos;


    State(char sym, int lineNo, long pos) {
        this.sym = sym;
        this.lineNo = lineNo;
        this.pos = pos;
    }

    public char getSym() {
        return sym;
    }

    public int getLineNo() {
        return lineNo;
    }

    public long getPos() {
        return pos;
    }
}
/**
 *  用if...else...判断顺序编写
 *  名字字符串先全部读取再判断是否是关键字
 *  其它的也是，基本不用倒回循环开头
 *  */
public class SequenceTokenizer implements Tokenizer {
    /**
     * 采用随机访问文件流
     */
    private final RandomAccessFile file;
    private char sym;
    private int lineNo;
    private Stack<State> states;

    /**
     * 输入文件的地址字符串
     */
    public SequenceTokenizer(String fileAddr) throws Exception {
        this.file = new RandomAccessFile(fileAddr, "r");
        this.sym = ' ';
        this.lineNo = 1;
        this.states = new Stack<>();
    }

    @Override
    public int getLineNo() {
        if (sym == '\n') {
            return lineNo - 1;
        }
        return lineNo;
    }

    /**
     *  关闭读取的文件
     * @throws IOException
     */
    @Override
    public void close() throws IOException{
        file.close();
    }

    /*
      一些辅助方法
     */

    /**
     *
     * @return boolean 为真表示读到了字符，没到文件尾
     * @throws IOException
     */
    private boolean readChar() throws IOException {
        try {
            sym = (char) file.readByte();
        } catch (IOException e) {
            if (e instanceof EOFException) {
                return false;
            } else {
                throw e;
            }
        }
        if (sym == '\n') {
            lineNo++;
        }
        return true;
    }

    /**
     * 跳过空白字符并读取第一个非空白字符
     * 如果sym本就非空白则直接返回
     * @return boolean 为真表示读到了非空字符，没到文件尾
     */
    private boolean skipBlank() throws IOException {
        while (Character.isWhitespace(sym)) {
            if (!readChar()) {
                sym = ' ';
                return false;
            }
        }
        return true;
    }

    /**
     * 跳过当前行，让sym指向下一行
     * @throws IOException
     */
    @Override
    public void skipLine() throws IOException {
        while (sym != '\n') {
            if (!readChar()) {
                sym = ' ';
                return;
            }
        }
        if (!readChar()) {
            sym = ' ';
        }
    }

    /**
     * 跳过多行直到"* /"或文件尾
     * @throws IOException
     */
    private void skipLines() throws IOException {
        while (true) {
            if (sym == '*') {
                if (!readChar()) {
                    sym = ' ';
                    return;
                }
                if (sym != '/') {
                    continue;
                }
                if (!readChar()) {
                    sym = ' ';
                }
                return;
            } else {
                if (!readChar()) {
                    sym = ' ';
                    return;
                }
            }
        }
    }

    /**
     * 读一个标识符的名字并返回
     * requires Character.isAlphabetic(sym)
     * @return String
     */
    private String readName() throws IOException {
        StringBuilder res =  new StringBuilder("");
        do {
            res.append(sym);
            if (!readChar()) {
                sym = ' ';
                break;
            }
        } while (Character.isAlphabetic(sym) || Character.isDigit(sym) || sym == '_');
        return res.toString();
    }

    /**
     * 读一个数字串
     * @return String
     * @throws IOException
     */
    private String readInt() throws IOException {
        StringBuilder res = new StringBuilder("");
        do {
            res.append(sym);
            if (!readChar()) {
                sym = ' ';
                break;
            }
        } while (Character.isDigit(sym));
        return res.toString();
    }

    /**
     * 读取格式化字符串，默认sym = '"'
     * 要求为32, 33, 40-126的ASCII字符且%, \字符只能有%d和\n搭配
     * @return String
     * @throws IOException
     * @throws FormatStringException
     */
    private FString readFormatString() throws Exception {
        StringBuilder res = new StringBuilder("");
        int num = 0;
        boolean illegal = false;
        char tmp;
        do {
            res.append(sym);
            if (!readChar()) {
                throw new FormatStringException("No matching \" at " + lineNo);
            }
            //先把sym记起来
            tmp = sym;
            // 检查sym是否为\或%
            if (tmp == '\\' || tmp == '%') {
                res.append(tmp);
                if (!readChar()) {
                    throw new FormatStringException("No matching with \" at " + lineNo);
                }
                // 检查是否为\n
                if (tmp == '\\' && sym != 'n') {
                    illegal = true;
                }
                // 检查是否为%d
                if (tmp == '%' && sym != 'd') {
                    illegal = true;
                }
                // 记录参数个数
                if (tmp == '%' && sym == 'd') {
                    num++;
                }
                continue;
            }
            // 检查结束符双引号
            if (tmp == '"') {
                res.append(tmp);
                if (!readChar()) {
                    sym = ' ';
                }
                break;
            }
            // 检查字符范围
            if (!(tmp == 32 || tmp == 33 || 40 <= tmp && tmp <= 126)) {
                illegal = true;
            }
        } while (true);
        return new FString(res.toString(), Category.STRCON, getLineNo(), num, illegal);
    }

    /**
     *
     * @return 返回下一个单词，同时指针也往下移动一次
     * @throws Exception IO等异常
     */
    @Override
    public Word next() throws Exception {
        while (true) {
            // 到达文件尾，直接结束，返回结束单词
            if (!skipBlank()) {
                return Word.END;
            }
            // 否则对sym分类讨论
            // sym为字母或下划线开头
            if (Character.isAlphabetic(sym) || sym == '_') {
                String name = readName();
                Word res = Word.KEY_WORD.get(name);
                // 是关键字直接返回相应的单词
                if (res != null) {
                    return new Word(name, res.getCategory(), getLineNo());
                }
                // 否则是标识符
                return new Word(name, Category.IDENFR, getLineNo());
            }
            // sym为数字开头
            if (Character.isDigit(sym)) {
                // 为0开头
                if (sym == '0') {
                    if (!readChar()) {
                        sym = ' ';
                        return new Word(Word.ZERO.getName(), Word.ZERO.getCategory(), getLineNo());
                    }
                    // 若有前导0则违法
                    if (Character.isDigit(sym)) {
                        throw new IntConstException("Redundant 0");
                    }
                    // 否则为0
                    return new Word(Word.ZERO.getName(), Word.ZERO.getCategory(), getLineNo());
                }
                // 为非0数字开头
                return new Word(readInt(), Category.INTCON, getLineNo());
            }
            // sym为!
            if (sym == '!') {
                if (!readChar()) {
                    sym = ' ';
                    return new Word(Word.NOT.getName(), Word.NOT.getCategory(), getLineNo());
                }
                // 为!=
                if (sym == '=') {
                    if (!readChar()) {
                        sym = ' ';
                    }
                    return new Word(Word.NEQ.getName(), Word.NEQ.getCategory(), getLineNo());
                }
                //否则为!
                return new Word(Word.NOT.getName(), Word.NOT.getCategory(), getLineNo());
            }
            // sym为"，一定是格式字符串
            // 要求为32, 33, 40-126的ASCII字符
            // 且%, \字符只能有%d和\n搭配
            if (sym == '"') {
                return readFormatString();
            }
            // sym为&
            if (sym == '&') {
                if (!readChar()) {
                    throw new AndException("No matching for &");
                }
                if (sym != '&') {
                    throw new AndException("No matching for &");
                }
                if (!readChar()) {
                    sym = ' ';
                }
                return new Word(Word.AND.getName(), Word.AND.getCategory(), getLineNo());
            }
            // sym为|
            if (sym == '|') {
                if (!readChar()) {
                    throw new OrException("No matching for |");
                }
                if (sym != '|') {
                    throw new OrException("No matching for |");
                }
                if (!readChar()) {
                    sym = ' ';
                }
                return new Word(Word.OR.getName(), Word.OR.getCategory(), getLineNo());
            }
            // sym为<
            if (sym == '<') {
                if (!readChar()) {
                    sym = ' ';
                    return new Word(Word.LST.getName(), Word.LST.getCategory(), getLineNo());
                }
                if (sym != '=') {
                    return new Word(Word.LST.getName(), Word.LST.getCategory(), getLineNo());
                }
                if (!readChar()) {
                    sym = ' ';
                }
                return new Word(Word.LEQ.getName(), Word.LEQ.getCategory(), getLineNo());
            }
            // sym为>
            if (sym == '>') {
                if (!readChar()) {
                    sym = ' ';
                    return new Word(Word.GRT.getName(), Word.GRT.getCategory(), getLineNo());
                }
                if (sym != '=') {
                    return new Word(Word.GRT.getName(), Word.GRT.getCategory(), getLineNo());
                }
                if (!readChar()) {
                    sym = ' ';
                }
                return new Word(Word.GEQ.getName(), Word.GEQ.getCategory(), getLineNo());
            }
            // sym为=
            if (sym == '=') {
                if (!readChar()) {
                    sym = ' ';
                    return new Word(Word.ASSIGN.getName(), Word.ASSIGN.getCategory(), getLineNo());
                }
                if (sym != '=') {
                    return new Word(Word.ASSIGN.getName(), Word.ASSIGN.getCategory(), getLineNo());
                }
                if (!readChar()) {
                    sym = ' ';
                }
                return new Word(Word.EQL.getName(), Word.EQL.getCategory(), getLineNo());
            }
            // sym为/，有可能是注释
            if (sym == '/') {
                if (!readChar()) {
                    sym = ' ';
                    return new Word(Word.DIV.getName(), Word.DIV.getCategory(), getLineNo());
                }
                // 为行注释
                if (sym == '/') {
                    skipLine();
                    continue;
                }
                // 为自由注释
                if (sym == '*') {
                    if (!readChar()) {
                        sym = ' ';
                        continue;
                    }
                    skipLines();
                    continue;
                }
                // 为除法
                return new Word(Word.DIV.getName(), Word.DIV.getCategory(), getLineNo());
            }
            // 其余的为简单单个字符
            char tmp = sym;
            if (!readChar()) {
                sym = ' ';
            }
            switch (tmp) {
                case '+': return new Word(Word.PLUS.getName(), Word.PLUS.getCategory(), getLineNo());
                case '-': return new Word(Word.MINUS.getName(), Word.MINUS.getCategory(), getLineNo());
                case '*': return new Word(Word.MULT.getName(), Word.MULT.getCategory(), getLineNo());
                case '%': return new Word(Word.MOD.getName(), Word.MOD.getCategory(), getLineNo());
                case ';': return new Word(Word.SEMICN.getName(), Word.SEMICN.getCategory(), getLineNo());
                case ',': return new Word(Word.COMMA.getName(), Word.COMMA.getCategory(), getLineNo());
                case '(': return new Word(Word.LPARENT.getName(), Word.LPARENT.getCategory(),
                        getLineNo());
                case ')': return new Word(Word.RPARENT.getName(), Word.RPARENT.getCategory(),
                        getLineNo());
                case '[': return new Word(Word.LBRK.getName(), Word.LBRK.getCategory(), getLineNo());
                case ']': return new Word(Word.RBRK.getName(), Word.RBRK.getCategory(), getLineNo());
                case '{': return new Word(Word.LBIG.getName(), Word.LBIG.getCategory(), getLineNo());
                case '}': return new Word(Word.RBIG.getName(), Word.RBIG.getCategory(), getLineNo());
                default: throw new IllegalCharacterException("No such character");
            }
        }
    }

    /**
     * requires num > 0
     * @param num 偷看下num个单词
     * @return 返回下num个单词的列表，指针不动
     * @throws Exception 异常同next
     */
    @Override
    public List<Word> peek(int num) throws Exception {
        List<Word> list = new ArrayList<>();
        peekStart();
        for (int i = 0; i < num; i++) {
            list.add(next());
        }
        peekEnd();
        return list;
    }

    private void setState(State state) throws IOException {
        sym = state.getSym();
        lineNo = state.getLineNo();
        file.seek(state.getPos());
    }

    @Override
    public void peekStart() throws IOException {
        states.push(new State(sym, lineNo, file.getFilePointer()));
    }

    @Override
    public void peekEnd() throws IOException {
        setState(states.pop());
    }

    @Override
    public void peekTo() throws IOException {
        states.pop();
    }

}
