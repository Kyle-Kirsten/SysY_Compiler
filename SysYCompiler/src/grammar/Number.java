package grammar;

import wordTokenizer.Word;

public class Number extends Node {
    private Word num;

    public Number(Word num) {
        this.num = num;
    }
}
