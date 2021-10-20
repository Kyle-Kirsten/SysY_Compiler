package wordTokenizer;

public class FString extends Word {
    private int num; /*参数个数*/
    private boolean illegal; /*表示是否有违法字符*/


    public FString(String name, Category category, int lineNo, int num, boolean illegal) {
        super(name, category, lineNo);
        this.num = num;
        this.illegal = illegal;
    }

    public int getNum() {
        return num;
    }

    public boolean isIllegal() {
        return illegal;
    }
}
