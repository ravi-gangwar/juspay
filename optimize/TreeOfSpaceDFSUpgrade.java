import java.util.*;

class Node {
    String name;
    Node parent;
    List<Node> children;
    boolean isLocked;
    int lockedBy;

    public Node(String name) {
        this.name = name;
        this.children = new ArrayList<>();
        this.isLocked = false;
        this.lockedBy = -1;
    }
}

public class TreeOfSpaceDFSUpgrade {
    private Map<String, Node> nodeMap = new HashMap<>();

    public TreeOfSpaceDFSUpgrade(List<String> nodeNames, Map<String, List<String>> adjList) {
        for (String name : nodeNames) {
            nodeMap.put(name, new Node(name));
        }

        for (String parentName : adjList.keySet()) {
            Node parent = nodeMap.get(parentName);
            for (String childName : adjList.get(parentName)) {
                Node child = nodeMap.get(childName);
                parent.children.add(child);
                child.parent = parent;
            }
        }
    }

    public boolean lock(String name, int uid) {
        Node node = nodeMap.get(name);
        if (node.isLocked || hasLockedAncestor(node) || hasLockedDescendant(node)) return false;

        node.isLocked = true;
        node.lockedBy = uid;
        return true;
    }

    public boolean unlock(String name, int uid) {
        Node node = nodeMap.get(name);
        if (!node.isLocked || node.lockedBy != uid) return false;

        node.isLocked = false;
        node.lockedBy = -1;
        return true;
    }

    public boolean upgrade(String name, int uid) {
        Node node = nodeMap.get(name);
        if (node.isLocked || hasLockedAncestor(node)) return false;

        List<Node> lockedNodes = new ArrayList<>();
        if (!collectLockedDescendants(node, uid, lockedNodes) || lockedNodes.isEmpty()) return false;

        for (Node locked : lockedNodes) {
            locked.isLocked = false;
            locked.lockedBy = -1;
        }

        node.isLocked = true;
        node.lockedBy = uid;
        return true;
    }

    private boolean hasLockedAncestor(Node node) {
        Node current = node.parent;
        while (current != null) {
            if (current.isLocked) return true;
            current = current.parent;
        }
        return false;
    }

    private boolean hasLockedDescendant(Node node) {
        for (Node child : node.children) {
            if (child.isLocked || hasLockedDescendant(child)) return true;
        }
        return false;
    }

    private boolean collectLockedDescendants(Node node, int uid, List<Node> result) {
        boolean found = false;
        for (Node child : node.children) {
            if (collectLockedDescendants(child, uid, result)) found = true;
        }
        if (node.isLocked) {
            if (node.lockedBy != uid) return false;
            result.add(node);
            return true;
        }
        return found;
    }

    public static void main(String[] args) {
        List<String> nodeNames = List.of("World", "Asia", "Africa", "China", "India", "SouthAfrica", "Egypt");
        Map<String, List<String>> adjList = new HashMap<>();
        adjList.put("World", List.of("Asia", "Africa"));
        adjList.put("Asia", List.of("China", "India"));
        adjList.put("Africa", List.of("SouthAfrica", "Egypt"));

        TreeOfSpaceDFSUpgrade tree = new TreeOfSpaceDFSUpgrade(nodeNames, adjList);

        System.out.println("Lock China (9): " + tree.lock("China", 9));
        System.out.println("Lock India (9): " + tree.lock("India", 9));
        System.out.println("Upgrade Asia (9): " + tree.upgrade("Asia", 9));
        System.out.println("Unlock India (9): " + tree.unlock("India", 9));  // false
        System.out.println("Unlock Asia (9): " + tree.unlock("Asia", 9));
        System.out.println("Lock Asia (10): " + tree.lock("Asia", 10));
        System.out.println("Lock World (10): " + tree.upgrade("World", 10));
    }
}
