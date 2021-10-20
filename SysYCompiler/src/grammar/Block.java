package grammar;

import wordTokenizer.Word;

import java.util.List;

public class Block extends Node {
    private List<Node> stmts;
    private Word rBig;

    public Block(List<Node> stmts) {
        this.stmts = stmts;
    }

    public Block(List<Node> stmts, Word rBig) {
        this(stmts);
        this.rBig = rBig;
    }

    public int getEndLine() {
        return rBig.getLineNo();
    }

    public List<Node> getStmts() {
        return stmts;
    }

    public Node getLastStmt() {
        if (stmts.isEmpty()) {
            return Node.unDefined;
        }
        return stmts.get(stmts.size() - 1);
    }
}
