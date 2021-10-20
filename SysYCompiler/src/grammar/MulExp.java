package grammar;

import wordTokenizer.Category;
import wordTokenizer.Word;

public class MulExp extends ValExp {
    private Node lval;
    private Node rval;

    public MulExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
        super.type = Category.INTTK;
    }
}
