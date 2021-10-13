package grammar;

public class IfExp extends Node {
    private Node cond;
    private Node ifBody;
    private Node elseBody;


    public IfExp(Node cond, Node ifBody, Node elseBody) {
        this.cond = cond;
        this.ifBody = ifBody;
        this.elseBody = elseBody;
    }
}
