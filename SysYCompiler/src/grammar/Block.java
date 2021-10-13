package grammar;

import java.util.List;

public class Block extends Node {
    private List<Node> stmts;

    public Block(List<Node> stmts) {
        this.stmts = stmts;
    }
}
