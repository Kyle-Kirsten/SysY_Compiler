package grammar;

import wordTokenizer.Category;

public class VarSymbol extends Symbol {
    private Category type;
    private int dims;

    protected VarSymbol(String name, Category type, int dims) {
        super(name);
        this.type = type;
        this.dims = dims;
    }

    @Override
    public Category getType() {
        return type;
    }

    public int getDims() {
        return dims;
    }
}
