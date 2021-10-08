package wordTokenizer;

import exceptions.*;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

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

    /**
     * 输入文件的地址字符串
     */
    public SequenceTokenizer(String fileAddr) throws FileNotFoundException {
        this.file = new RandomAccessFile(fileAddr, "r");
        this.sym = ' ';
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
     * 跳过当前行，让sym指下下一行
     * @throws IOException
     */
    private void skipLine() throws IOException {
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
    private String readFormatString() throws IOException, FormatStringException {
        StringBuilder res = new StringBuilder("");
        char tmp;
        do {
            res.append(sym);
            if (!readChar()) {
                throw new FormatStringException("No matching \"");
            }
            //先把sym记起来
            tmp = sym;
            // 检查sym是否为\或%
            if (tmp == '\\' || tmp == '%') {
                res.append(tmp);
                if (!readChar()) {
                    throw new FormatStringException("No matching with \"");
                }
                // 检查是否为\n
                if (tmp == '\\' && sym != 'n') {
                    throw new FormatStringException("Wrong matching with \\");
                }
                // 检查是否为%d
                if (tmp == '%' && sym != 'd') {
                    throw new FormatStringException("Wrong matching with %");
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
                throw new FormatStringException("Wrong character");
            }

        } while (true);
        return res.toString();
    }

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
                    return res;
                }
                // 否则是标识符
                return new Word(name, Category.IDENFR);
            }
            // sym为数字开头
            if (Character.isDigit(sym)) {
                // 为0开头
                if (sym == '0') {
                    if (!readChar()) {
                        sym = ' ';
                        return Word.ZERO;
                    }
                    // 若有前导0则违法
                    if (Character.isDigit(sym)) {
                        throw new IntConstException("Redundant 0");
                    }
                    // 否则为0
                    return Word.ZERO;
                }
                // 为非0数字开头
                return new Word(readInt(), Category.INTCON);
            }
            // sym为!
            if (sym == '!') {
                if (!readChar()) {
                    sym = ' ';
                    return Word.NOT;
                }
                // 为!=
                if (sym == '=') {
                    if (!readChar()) {
                        sym = ' ';
                    }
                    return Word.NEQ;
                }
                //否则为!
                return Word.NOT;
            }
            // sym为"，一定是格式字符串
            // 要求为32, 33, 40-126的ASCII字符
            // 且%, \字符只能有%d和\n搭配
            if (sym == '"') {
                return new Word(readFormatString(), Category.STRCON);
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
                return Word.AND;
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
                return Word.OR;
            }
            // sym为<
            if (sym == '<') {
                if (!readChar()) {
                    sym = ' ';
                    return Word.LST;
                }
                if (sym != '=') {
                    return Word.LST;
                }
                if (!readChar()) {
                    sym = ' ';
                }
                return Word.LEQ;
            }
            // sym为>
            if (sym == '>') {
                if (!readChar()) {
                    sym = ' ';
                    return Word.GRT;
                }
                if (sym != '=') {
                    return Word.GRT;
                }
                if (!readChar()) {
                    sym = ' ';
                }
                return Word.GEQ;
            }
            // sym为=
            if (sym == '=') {
                if (!readChar()) {
                    sym = ' ';
                    return Word.ASSIGN;
                }
                if (sym != '=') {
                    return Word.ASSIGN;
                }
                if (!readChar()) {
                    sym = ' ';
                }
                return Word.EQL;
            }
            // sym为/，有可能是注释
            if (sym == '/') {
                if (!readChar()) {
                    sym = ' ';
                    return Word.DIV;
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
                return Word.DIV;
            }
            // 其余的为简单单个字符
            char tmp = sym;
            if (!readChar()) {
                sym = ' ';
            }
            switch (tmp) {
                case '+': return Word.PLUS;
                case '-': return Word.MINUS;
                case '*': return Word.MULT;
                case '%': return Word.MOD;
                case ';': return Word.SEMICN;
                case ',': return Word.COMMA;
                case '(': return Word.LPARENT;
                case ')': return Word.RPARENT;
                case '[': return Word.LBRK;
                case ']': return Word.RBRK;
                case '{': return Word.LBIG;
                case '}': return Word.RBIG;
                default: throw new IllegalCharacterException("No such character");
            }
        }
    }
}
