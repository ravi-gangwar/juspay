// Approach 1: Naive Tree Traversal
// Lock, Unlock, and Upgrade operations using full ancestor and descendant traversals

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

public class TreeOfSpaceNaive {
    private Map<String, Node> nodeMap = new HashMap<>(); // map with node name with the creating new node object

    public TreeOfSpaceNaive(List<String> nodeNames, Map<String, List<String>> adjList) {
        for (String name : nodeNames) {
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
    // O (n) as we are traversing the entire tree
    public boolean lock(String name, int uid) {
        Node node = nodeMap.get(name);
        if (node == null || node.isLocked || hasLockedAncestor(node) || hasLockedDescendant(node)) {
            return false; // if the node is null or the node is locked or the node has a locked ancestor or the node has a locked descendant, return false
        }
        node.isLocked = true; // set the node to locked
        node.lockedBy = uid; // set the lockedBy to the uid
        return true;
    }

    public boolean unlock(String name, int uid) {
        Node node = nodeMap.get(name);
        if (node == null || !node.isLocked || node.lockedBy != uid) {
            return false; // if the node is null or the node is not locked or the lockedBy is not the uid, return false
        }
        node.isLocked = false; // set the node to unlocked
        node.lockedBy = -1; // set the lockedBy to -1
        return true;
    }

    public boolean upgrade(String name, int uid) {
        Node node = nodeMap.get(name);
        if (node == null || node.isLocked || hasLockedAncestor(node)) {
            return false; // if the node is null or the node is locked or the node has a locked ancestor, return false
        }

        List<Node> lockedDescendants = new ArrayList<>();
        if (!collectLockedDescendantsByUid(node, uid, lockedDescendants) || lockedDescendants.isEmpty()) {
            return false; // if the node has no locked descendants or the locked descendants are not the uid, return false
        }

        // Unlock all valid descendants
        for (Node desc : lockedDescendants) {
            desc.isLocked = false; // set the node to unlocked
            desc.lockedBy = -1; // set the lockedBy to -1
        }

        // Lock the target node
        node.isLocked = true; // set the node to locked
        node.lockedBy = uid; // set the lockedBy to the uid
        return true;
    }

    private boolean hasLockedAncestor(Node node) { // check if the node has a locked ancestor
        Node current = node.parent; // get the parent node
        while (current != null) { // while the current node is not null
            if (current.isLocked) return true; // if the current node is locked, return true
            current = current.parent; // get the parent node
        }
        return false; // if the node has no locked ancestor, return false
    }

    private boolean hasLockedDescendant(Node node) { // check if the node has a locked descendant
        for (Node child : node.children) { // for each child node in the node
            if (child.isLocked || hasLockedDescendant(child)) { // if the child node is locked or the child node has a locked descendant, return true
                return true; // if the node has a locked descendant, return true
            }
        }
        return false; // if the node has no locked descendant, return false
    }

    private boolean collectLockedDescendantsByUid(Node node, int uid, List<Node> result) { // collect the locked descendants by the uid
        boolean valid = true; // set the valid to true
        if (node.isLocked) { // if the node is locked
            if (node.lockedBy != uid) return false; // if the lockedBy is not the uid, return false
            result.add(node); // add the node to the result
        }
        for (Node child : node.children) {
            boolean res = collectLockedDescendantsByUid(child, uid, result);
            if (!res) valid = false;
        }
        return valid;
    }

    public static void main(String[] args) {
        List<String> nodeNames = List.of("World", "Asia", "Africa", "China", "India", "SouthAfrica", "Egypt");
        Map<String, List<String>> adjList = new HashMap<>();
        adjList.put("World", List.of("Asia", "Africa"));
        adjList.put("Asia", List.of("China", "India"));
        adjList.put("Africa", List.of("SouthAfrica", "Egypt"));

        TreeOfSpaceNaive tree = new TreeOfSpaceNaive(nodeNames, adjList);

        System.out.println("Lock China (9): " + tree.lock("China", 9));
        System.out.println("Lock India (9): " + tree.lock("India", 9));
        System.out.println("Upgrade Asia (9): " + tree.upgrade("Asia", 9));
        System.out.println("Unlock India (9): " + tree.unlock("India", 9));
        System.out.println("Unlock Asia (9): " + tree.unlock("Asia", 9));
        System.out.println("Lock World (10): " + tree.lock("World", 10));
        System.out.println("Lock Asia (10): " + tree.lock("Asia", 10));
    }
}
