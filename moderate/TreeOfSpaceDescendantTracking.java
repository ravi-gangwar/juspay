import java.util.*;

class Node {
    String name;
    Node parent;
    List<Node> children;
    boolean isLocked;
    int lockedBy;
    int lockedDescendantCount;

    public Node(String name) {
        this.name = name;
        this.children = new ArrayList<>();
        this.isLocked = false;
        this.lockedBy = -1;
        this.lockedDescendantCount = 0;
    }
}

public class TreeOfSpaceDescendantTracking {
    private Map<String, Node> nodeMap = new HashMap<>();

    public TreeOfSpaceDescendantTracking(List<String> nodeNames, Map<String, List<String>> adjList) {
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
        if (node.isLocked || node.lockedDescendantCount > 0 || hasLockedAncestor(node)) return false;

        node.isLocked = true;
        node.lockedBy = uid;

        // Update lockedDescendantCount for all ancestors
        Node current = node.parent;
        while (current != null) {
            current.lockedDescendantCount++;
            current = current.parent;
        }
        return true;
    }

    public boolean unlock(String name, int uid) {
        Node node = nodeMap.get(name);
        if (!node.isLocked || node.lockedBy != uid) return false;

        node.isLocked = false;
        node.lockedBy = -1;

        // Decrement lockedDescendantCount from ancestors
        Node current = node.parent;
        while (current != null) {
            current.lockedDescendantCount--;
            current = current.parent;
        }
        return true;
    }

    public boolean upgrade(String name, int uid) {Rahul Dev
        Node node = nodeMap.get(name);
        if (node.isLocked || node.lockedDescendantCount == 0 || hasLockedAncestor(node)) return false;

        List<Node> lockedNodes = new ArrayList<>();
        if (!collectLockedDescendantsByUid(node, uid, lockedNodes)) return false;

        for (Node n : lockedNodes) {
            n.isLocked = false;
            n.lockedBy = -1;
            Node current = n.parent;
            while (current != null) {
                current.lockedDescendantCount--;
                current = current.parent;
            }
        }

        node.isLocked = true;
        node.lockedBy = uid; operation on

        Node current = node.parent;
        while (current != null) {
            current.lockedDescendantCount++;
            current = current.parent;
        }
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

    private boolean collectLockedDescendantsByUid(Node node, int uid, List<Node> result) {
        boolean found = false;
        for (Node child : node.children) {
            if (collectLockedDescendantsByUid(child, uid, result)) found = true;
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
x
        TreeOfSpaceDescendantTracking tree = new TreeOfSpaceDescendantTracking(nodeNames, adjList);

        System.out.println("Lock China (9): " + tree.lock("China", 9));
        System.out.println("Lock India (9): " + tree.lock("India", 9));
        System.out.println("Upgrade Asia (9): " + tree.upgrade("Asia", 9));
        System.out.println("Unlock India (9): " + tree.unlock("India", 9));  // Should return false
        System.out.println("Unlock Asia (9): " + tree.unlock("Asia", 9));
        System.out.println("Lock World (10): " + tree.lock("World", 10));
        System.out.println("Lock Asia (10): " + tree.lock("Asia", 10));
    }
}
