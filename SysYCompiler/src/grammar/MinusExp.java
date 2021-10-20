package grammar;

import wordTokenizer.Category;
import wordTokenizer.Word;

public class MinusExp extends ValExp {
    private Node val;

    public MinusExp(Node val) {
        this.val = val;
        super.type = Category.INTTK;
    }
}
