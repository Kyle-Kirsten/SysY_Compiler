package grammar;

public class DivExp extends Node {
    private Node lval;
    private Node rval;


    public DivExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
    }
}
