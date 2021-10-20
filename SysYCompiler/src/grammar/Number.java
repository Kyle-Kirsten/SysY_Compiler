package grammar;

import wordTokenizer.Category;
import wordTokenizer.Word;

public class Number extends ValExp {
    private Word num;

    public Number(Word num) {
        this.num = num;
        type = Category.INTTK;
    }
}
