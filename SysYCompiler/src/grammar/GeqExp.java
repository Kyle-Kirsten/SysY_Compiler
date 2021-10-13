package grammar;

public class GeqExp extends Node {
    private Node lval;
    private Node rval;


    public GeqExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
    }
}
