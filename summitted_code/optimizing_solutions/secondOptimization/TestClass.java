package summitted_code.optimizing_solutions.secondOptimization;


import java.io.*;
import java.util.*;

class Node {
    String name;
    Node parent;
    List<Node> children;
    boolean isLocked;
    int lockedBy;
    Set<Node> lockedDescendants;
    Map<Integer, Integer> descendantLockHistoryByUid;

    public Node(String name){
        this.name = name;
        this.children = new ArrayList<>();
        this.isLocked = false;
        this.lockedBy = -1;
        this.lockedDescendants = new HashSet<>();
        this.descendantLockHistoryByUid = new HashMap<>();
    }
}

class TestClass {
    static class TreeOfSpaceMySolution{

        private Map<String, Node> nodeMap = new HashMap<>();

        public TreeOfSpaceMySolution(List<String> nodeNames, int m){
            // O(n) - Create all nodes
            for(String name : nodeNames){
                nodeMap.put(name, new Node(name));
            }

            // O(n) - Build tree structure
            int idx = 1;
            for(int i = 0; i < nodeNames.size(); i++){
                Node parent = nodeMap.get(nodeNames.get(i));
                for(int j = 0; j < m && idx < nodeNames.size(); j++){
                    Node child = nodeMap.get(nodeNames.get(idx++));
                    parent.children.add(child);
                    child.parent = parent;
                }
            }
        }

        /*
         * SECOND OPTIMIZATION - Time Complexity Analysis:
         * 
         * Key Improvements:
         * - Added descendantLockHistoryByUid to track user-specific lock counts
         * - Optimized upgrade validation using user-specific counters
         * - Maintained O(h) ancestor operations but with better upgrade logic
         * 
         * Complexity notation:
         * - h: height from the node to the root
         * - n_subtree: number of nodes in the node's subtree
         * - k: number of locked descendants of the node
         * - u: number of unique users with locks in subtree
         * Assumptions: HashMap/HashSet ops are average-case O(1).
         */

        public boolean lock(String name, int userId){
            // O(1) - HashMap lookup
            Node node = nodeMap.get(name);
            
            // O(1) - Check if node is already locked
            if(node.isLocked) return false;
            
            // O(h) - Check if any ancestor is locked (linear ancestor traversal)
            if(hasLockedAncestor(node)) return false;
            
            // O(1) - Check if any descendants are locked (using HashSet)
            if(node.lockedDescendants.size() > 0) return false;

            // O(1) - Lock the node
            node.isLocked = true;
            // O(1) - Set the user ID
            node.lockedBy = userId;
            
            // O(h) - Update all ancestors' locked descendants sets and user history
            updateDescendantsOfAncestor(node, node, true);
            
            return true;
        }

        private boolean updateDescendantsOfAncestor(Node node, Node lockedNode, boolean isAdded){
            // O(h) - Walk up the ancestor chain
            Node current = node.parent;
            while(current != null){
                // O(1) - Add/remove from locked descendants set
                if(isAdded) current.lockedDescendants.add(lockedNode);
                else current.lockedDescendants.remove(lockedNode);
                
                // O(1) - Update user-specific lock count
                if(isAdded) {
                    // O(1) - Increment user's lock count
                    current.descendantLockHistoryByUid.put(lockedNode.lockedBy, 
                        current.descendantLockHistoryByUid.getOrDefault(lockedNode.lockedBy, 0) + 1);
                } else {
                    // O(1) - Decrement user's lock count
                    if(current.descendantLockHistoryByUid.get(lockedNode.lockedBy) == 1) {
                        // O(1) - Remove user if count becomes 0
                        current.descendantLockHistoryByUid.remove(lockedNode.lockedBy);
                    } else {
                        // O(1) - Decrement count
                        current.descendantLockHistoryByUid.put(lockedNode.lockedBy, 
                            current.descendantLockHistoryByUid.get(lockedNode.lockedBy) - 1);
                    }
                }
                // O(1) - Move to parent
                current = current.parent;
            }
            return true;
        }

        public boolean unlock(String name, int userId){
            // O(1) - HashMap lookup
            Node node = nodeMap.get(name);
            
            // O(1) - Check ownership and lock status
            if(!node.isLocked || node.lockedBy != userId) return false;

            // O(1) - Unlock the node
            node.isLocked = false;
            // O(1) - Clear user ID
            node.lockedBy = -1;
            
            // O(h) - Remove from all ancestors' sets and update user history
            updateDescendantsOfAncestor(node, node, false);
            
            return true;
        }

        public boolean upgrade(String name, int userId){
            // O(1) - HashMap lookup
            Node node = nodeMap.get(name);
            
            // O(1) - Check if node exists and is not locked
            if(node == null || node.isLocked) return false;
            
            // O(h) - Check if any ancestor is locked
            if(hasLockedAncestor(node)) return false;

            // O(1) - Check if there are any locked descendants
            if(node.lockedDescendants.isEmpty()) return false;
            
            // O(1) - Check if only one user has locks in subtree
            if(node.descendantLockHistoryByUid.size() != 1) return false;
            
            // O(1) - Check if that user is the requesting user
            if(node.descendantLockHistoryByUid.get(userId) == 0) return false;

            // O(k) - Unlock all locked descendants
            for(Node n : node.lockedDescendants){
                // O(1) - Unlock descendant
                n.isLocked = false;
                // O(1) - Clear user ID
                n.lockedBy = -1;
                // O(1) - Clear descendant's locked descendants set
                n.lockedDescendants.clear();
                // O(1) - Clear descendant's user history
                n.descendantLockHistoryByUid.clear();
                // O(h) - Remove from all ancestors' sets and update user history
                updateDescendantsOfAncestor(n, n, false); 
                //TODO: here some improvement can be done here we can also to bulk update the ancestors
            }

            // O(1) - Lock the current node
            node.isLocked = true;
            // O(1) - Set the user ID
            node.lockedBy = userId;
            // O(1) - Clear current node's user history
            node.descendantLockHistoryByUid.clear();
            // O(1) - Clear current node's locked descendants set
            node.lockedDescendants.clear();
            
            // O(h) - Add to all ancestors' sets and update user history
            updateDescendantsOfAncestor(node, node, true);
            
            return true;
        }

        // O(h) - Check for locked ancestor using linear traversal
        private boolean hasLockedAncestor(Node node){
            Node current = node.parent;
            while(current != null){
                // O(1) - Check if current ancestor is locked
                if(current.isLocked) return true;
                // O(1) - Move to parent
                current = current.parent;
            }
            return false;
        }

        // O(n_subtree) - DFS to verify all locked nodes belong to user
        private boolean collectingLockedDescendantByUid(Node node, int userId){
            // O(1) - Check current node
            if(node.isLocked && node.lockedBy != userId) return false;
            
            // O(children) - Check all children recursively
            for(Node child : node.children){
                if(!collectingLockedDescendantByUid(child, userId)) return false;
            }
            return true;
        }
    }

    public static void main(String args[] ) throws Exception {
        // O(1) - Read input parameters
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        int n = Integer.parseInt(br.readLine());
        int m = Integer.parseInt(br.readLine());
        int q = Integer.parseInt(br.readLine());

        // O(n) - Read node names
        List<String> nodeNames = new ArrayList<>();
        for(int i = 0; i < n; i++){
            nodeNames.add(br.readLine().trim());
        }

        // O(n) - Build tree structure
        TreeOfSpaceMySolution tree = new TreeOfSpaceMySolution(nodeNames, m);

        // O(q) - Process queries
        for(int i = 0; i < q; i++){
            // O(1) - Parse query
            String[] parts = br.readLine().trim().split("\\s+");
            int type = Integer.parseInt(parts[0]);
            String name = parts[1];
            int userId = Integer.parseInt(parts[2]);

            // O(operation) - Execute operation based on type
            boolean result = switch(type){
                case 1 -> tree.lock(name, userId);      // O(h)
                case 2 -> tree.unlock(name, userId);    // O(h)
                case 3 -> tree.upgrade(name, userId);   // O(k·h)
                default -> false;
            };

            System.out.println(result);
        }
    }
}