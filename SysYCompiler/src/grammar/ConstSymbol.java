package grammar;

import wordTokenizer.Category;

public class ConstSymbol extends Symbol {
    private Category type;
    private int dims;

    public ConstSymbol(String name, Category type, int dims) {
        super(name);
        this.type = type;
        this.dims = dims;
    }

    @Override
    public Category getType() {
        return type;
    }

    @Override
    public int getDims() {
        return dims;
    }
}
