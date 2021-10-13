package grammar;

public class WhileExp extends Node {
    private Node cond;
    private Node body;


    public WhileExp(Node cond, Node body) {
        this.cond = cond;
        this.body = body;
    }
}
