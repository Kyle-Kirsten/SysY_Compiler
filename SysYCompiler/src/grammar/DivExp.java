package grammar;

import wordTokenizer.Category;
import wordTokenizer.Word;

public class DivExp extends ValExp {
    private Node lval;
    private Node rval;


    public DivExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
        super.type = Category.INTTK;
    }
}
