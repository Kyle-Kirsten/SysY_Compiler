package wordTokenizer;

import java.io.IOException;
import java.util.List;

public interface Tokenizer {
    /** 给出下一个单词
    *   到末尾时返回结束类别码
    *   有可能需要错误处理抛出异常
     */
    public Word next() throws Exception;

    public void close() throws Exception;

    public List<Word> peek(int num) throws Exception;

    public void peekStart() throws IOException;

    public void peekEnd() throws IOException;

}
