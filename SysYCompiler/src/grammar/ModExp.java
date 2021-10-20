package grammar;

import wordTokenizer.Category;
import wordTokenizer.Word;

public class ModExp extends ValExp {
    private Node lval;
    private Node rval;

    public ModExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
        super.type = Category.INTTK;
    }
}
