package grammar;

import java.util.List;

public class ArrayExp extends Node {
    private List<Node> elems;

    public ArrayExp(List<Node> elems) {
        this.elems = elems;
    }
}
