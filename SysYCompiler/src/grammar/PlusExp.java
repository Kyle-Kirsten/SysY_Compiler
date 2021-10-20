package grammar;

import wordTokenizer.Category;
import wordTokenizer.Word;

public class PlusExp extends ValExp {
    private Node val;

    public PlusExp(Node val) {
        this.val = val;
        super.type = Category.INTTK;
    }
}
