package grammar;

public class NotExp extends Node {
    private Node val;

    public NotExp(Node val) {
        this.val = val;
    }
}
