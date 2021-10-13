package grammar;

/**
 *
 */
public class AddExp extends Node {
    private Node lval;
    private Node rval;

    public AddExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
    }

}
