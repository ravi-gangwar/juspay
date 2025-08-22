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
    private Map<String, Node> nodeMap = new HashMap<>(); // map with node name with the creating new node object

    public TreeOfSpaceDescendantTracking(List<String> nodeNames, Map<String, List<String>> adjList) {
        for (String name : nodeNames) { // for each node name in the nodeNames
            nodeMap.put(name, new Node(name)); // put the node name with the creating new node object
        }

        for (String parentName : adjList.keySet()) { // for each parent name in the adjList
            Node parent = nodeMap.get(parentName); // get the parent node
            for (String childName : adjList.get(parentName)) { // for each child name in the adjList
                Node child = nodeMap.get(childName); // get the child node
                parent.children.add(child); // add the child node to the parent node
                child.parent = parent; // set the parent node to the child node
            }
        }
    }

    public boolean lock(String name, int uid) {
        Node node = nodeMap.get(name); // get the node
        if (node.isLocked || node.lockedDescendantCount > 0 || hasLockedAncestor(node)) return false; // if the node is locked or the node has a locked descendant or the node has a locked ancestor, return false

        node.isLocked = true; // set the node to locked
        node.lockedBy = uid; // set the lockedBy to the uid

        // Update lockedDescendantCount for all ancestors
        Node current = node.parent; // get the parent node
        while (current != null) { // while the current node is not null
            current.lockedDescendantCount++; // increment the lockedDescendantCount
            current = current.parent; // get the parent node
        }
        return true;
    }

    public boolean unlock(String name, int uid) {
            Node node = nodeMap.get(name); // get the node
        if (!node.isLocked || node.lockedBy != uid) return false; // if the node is not locked or the lockedBy is not the uid, return false

        node.isLocked = false; // set the node to unlocked
        node.lockedBy = -1; // set the lockedBy to -1

        // Decrement lockedDescendantCount from ancestors
        Node current = node.parent; // get the parent node
        while (current != null) { // while the current node is not null
            current.lockedDescendantCount--; // decrement the lockedDescendantCount
            current = current.parent; // get the parent node
        }
        return true;
    }

    public boolean upgrade(String name, int uid) {
        Node node = nodeMap.get(name); // get the node
        if (node.isLocked || node.lockedDescendantCount == 0 || hasLockedAncestor(node)) return false; // if the node is locked or the node has no locked descendants or the node has a locked ancestor, return false

        List<Node> lockedNodes = new ArrayList<>(); // create a list to store the locked nodes
        if (!collectLockedDescendantsByUid(node, uid, lockedNodes)) return false; // if the node has no locked descendants or the locked descendants are not the uid, return false

        for (Node n : lockedNodes) { // for each node in the lockedNodes
            n.isLocked = false; // set the node to unlocked
            n.lockedBy = -1; // set the lockedBy to -1
            Node current = n.parent; // get the parent node
            while (current != null) { // while the current node is not null
                current.lockedDescendantCount--; // decrement the lockedDescendantCount
                current = current.parent; // get the parent node
            }
        }

        node.isLocked = true; // set the node to locked
        node.lockedBy = uid; // set the lockedBy to the uid

        Node current = node.parent; // get the parent node
        while (current != null) { // while the current node is not null
            current.lockedDescendantCount++; // increment the lockedDescendantCount
            current = current.parent; // get the parent node
        }
        return true;
    }

    private boolean hasLockedAncestor(Node node) { // check if the node has a locked ancestor
        Node current = node.parent; // get the parent node
        while (current != null) { // while the current node is not null
            if (current.isLocked) return true; // if the current node is locked, return true
            current = current.parent; // get the parent node
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
