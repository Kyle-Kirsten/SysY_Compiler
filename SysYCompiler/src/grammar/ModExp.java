package grammar;

public class ModExp extends Node {
    private Node lval;
    private Node rval;

    public ModExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
    }
}
