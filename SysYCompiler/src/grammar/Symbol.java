package grammar;


import wordTokenizer.Category;

public abstract class Symbol {
    public final static Symbol undefined = new Symbol() {};
    protected String name;

    public Symbol(String name) {
        this.name = name;
    }

    public Symbol() {

    }

    public int getDims() {
        return 0;
    }

    public Category getType() {
        return Category.VOIDTK;
    }
}
