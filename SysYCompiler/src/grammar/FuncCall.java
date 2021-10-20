package grammar;

import wordTokenizer.Category;
import wordTokenizer.Word;

import java.util.List;

public class FuncCall extends ValExp {
    private Word ident;
    private List<Node> params;
    private Symbol symbol;

    public FuncCall(Word ident, List<Node> params) {
        this.ident = ident;
        this.params = params;
    }

    public FuncCall(Word ident, List<Node> params, Category type) {
        this(ident, params);
        super.type = type;
    }
}
