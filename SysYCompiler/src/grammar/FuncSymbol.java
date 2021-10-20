package grammar;

import wordTokenizer.Category;

import java.util.List;

public class FuncSymbol extends Symbol {
    private Category returnType;
    private List<VarSymbol> params;


    public FuncSymbol(String name, Category returnType, List<VarSymbol> params) {
        super(name);
        this.returnType = returnType;
        this.params = params;
    }

    public Category getReturnType() {
        return returnType;
    }

    @Override
    public Category getType() {
        return returnType;
    }

    @Override
    public int getDims() {
        return 0;
    }

    public List<VarSymbol> getParams() {
        return params;
    }
}
