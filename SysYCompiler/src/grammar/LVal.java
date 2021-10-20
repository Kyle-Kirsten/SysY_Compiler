package grammar;

import wordTokenizer.Category;
import wordTokenizer.Word;

import java.util.List;

public class LVal extends ValExp {
    private Word ident;
    private List<ValExp> index;
    private Symbol symbol;


    public LVal(Word ident, List<ValExp> index) {
        this.ident = ident;
        this.index = index;
        this.symbol = Symbol.undefined;
    }

    public LVal(Word ident, List<ValExp> index, Symbol symbol) {
        this(ident, index);
        this.symbol = symbol;
    }

    @Override
    public Category getType() {
        if (symbol == Symbol.undefined) {
            return Category.VOIDTK;
        }
        return symbol.getType();
    }

    @Override
    public int getDims() {
        if (symbol == Symbol.undefined) {
            return 0;
        }
        return symbol.getDims() - getIndexNum();
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public String getName() {
        return ident.getName();
    }

    public int getIndexNum() {
        return index.size();
    }

    public Word getIdent() {
        return ident;
    }

    public int getLineNo() {
        return ident.getLineNo();
    }
}
