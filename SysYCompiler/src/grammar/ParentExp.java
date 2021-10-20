package grammar;

public class ParentExp extends ValExp {
    private ValExp val;

    public ParentExp(ValExp val) {
        this.val = val;
        super.type = val.getType();
    }
}
