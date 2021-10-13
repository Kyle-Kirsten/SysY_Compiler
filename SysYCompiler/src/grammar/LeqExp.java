package grammar;

public class LeqExp extends Node {
    private Node lval;
    private Node rval;

    public LeqExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
    }
}
