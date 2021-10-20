package grammar;

import wordTokenizer.Category;

public abstract class ValExp extends Node {
    protected int dims = 0;
    protected Category type = Category.VOIDTK;

    public Category getType() {
        return type;
    }

    public int getDims() {
        return dims;
    }
}
