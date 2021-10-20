package grammar;

import wordTokenizer.Category;
import wordTokenizer.Word;

public class SubExp extends ValExp {
    private Node lval;
    private Node rval;

    public SubExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
        super.type = Category.INTTK;
    }
}
