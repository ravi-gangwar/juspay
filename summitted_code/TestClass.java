package summitted_code;

import java.io.*;
import java.util.*;

class Node {
    String name;
    Node parent;
    List<Node> children;
    boolean isLocked;
    int lockedBy;
    Set<Node> lockedDescendants; // Track locked descendants for O(1) check
    int depth; // Store depth for binary lifting

    public Node(String name){
        this.name = name;
        this.children = new ArrayList<>();
        this.isLocked = false;
        this.lockedBy = -1;
        this.lockedDescendants = new HashSet<>();
        this.depth = 0;
    }
}

public class TestClass {
    static class TreeOfSpaceMySolution{

        private Map<String, Node> nodeMap = new HashMap<>();
        private Node[][] ancestor; // Binary lifting table: ancestor[i][j] = 2^j th ancestor of node i
        private int maxLog; // Maximum log value needed
        private List<String> nodeNames; // Store node names for indexing

        public TreeOfSpaceMySolution(List<String> nodeNames, int m){
            this.nodeNames = nodeNames;
            
            // O(n) - Create nodes
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
            
            // O(n) - Calculate depths using DFS
            calculateDepths(nodeMap.get(nodeNames.get(0)), 0);
            
            // O(n log h) - Precompute binary lifting table
            precomputeBinaryLifting();
        }
        
        // O(n) - Calculate depth of each node using DFS
        private void calculateDepths(Node node, int depth) {
            node.depth = depth;
            for(Node child : node.children) {
                calculateDepths(child, depth + 1);
            }
        }
        
        // O(n log h) - Precompute binary lifting table
        private void precomputeBinaryLifting() {
            int n = nodeNames.size();
            maxLog = (int) Math.ceil(Math.log(n) / Math.log(2));
            ancestor = new Node[n][maxLog + 1];
            
            // Initialize first ancestor (parent)
            for(int i = 0; i < n; i++) {
                Node node = nodeMap.get(nodeNames.get(i));
                ancestor[i][0] = node.parent;
            }
            
            // Fill the table using dynamic programming
            for(int j = 1; j <= maxLog; j++) {
                for(int i = 0; i < n; i++) {
                    Node node = ancestor[i][j-1];
                    if(node != null) {
                        int parentIndex = nodeNames.indexOf(node.name);
                        ancestor[i][j] = ancestor[parentIndex][j-1];
                    } else {
                        ancestor[i][j] = null;
                    }
                }
            }
        }
        
        // O(log h) - Find kth ancestor using binary lifting
        private Node getKthAncestor(Node node, int k) {
            int nodeIndex = nodeNames.indexOf(node.name);
            for(int j = 0; j <= maxLog && node != null; j++) {
                if((k & (1 << j)) != 0) {
                    node = ancestor[nodeIndex][j];
                    if(node != null) {
                        nodeIndex = nodeNames.indexOf(node.name);
                    }
                }
            }
            return node;
        }

        /*
         * SECOND OPTIMIZATION - Time Complexity Analysis:
         * 
         * Binary Lifting Benefits:
         * - Ancestor operations: O(h) → O(log h)
         * - Memory: O(n log h) for ancestor table
         * - Preprocessing: O(n log h) one-time cost
         */

        public boolean lock(String name, int userId){
            // O(1) - HashMap lookup
            Node node = nodeMap.get(name);
            
            // O(1) - Check if node is locked
            if(node.isLocked) return false;
            
            // O(log h) - Check for locked ancestor using binary lifting
            if(hasLockedAncestorOptimized(node)) return false;
            
            // O(1) - Check if any descendants are locked
            if(!node.lockedDescendants.isEmpty()) return false;

            // O(1) - Lock the node
            node.isLocked = true;
            node.lockedBy = userId;
            
            // O(log h) - Update all ancestors' locked descendants sets
            updateAncestorsLockedDescendants(node, node, true);
            
            return true;
        }

        public boolean unlock(String name, int userId){
            // O(1) - HashMap lookup
            Node node = nodeMap.get(name);
            
            // O(1) - Check ownership and lock status
            if(!node.isLocked || node.lockedBy != userId) return false;

            // O(1) - Unlock the node
            node.isLocked = false;
            node.lockedBy = -1;
            
            // O(log h) - Remove from all ancestors' locked descendants sets
            updateAncestorsLockedDescendants(node, node, false);
            
            return true;
        }

        public boolean upgrade(String name, int userId){
            // O(1) - HashMap lookup
            Node node = nodeMap.get(name);
            
            // O(1) - Check if node exists and is not locked
            if(node == null || node.isLocked) return false;
            
            // O(log h) - Check for locked ancestor using binary lifting
            if(hasLockedAncestorOptimized(node)) return false;

            // O(1) - Check if there are any locked descendants
            if(node.lockedDescendants.isEmpty()) return false;
            
            // O(n_subtree) - Verify all locked descendants belong to user
            if(!collectingLockedDescendantByUidOptimized(node, userId)) return false;

            // O(k log h) - Unlock all locked descendants and update ancestors
            for(Node lockedDesc : node.lockedDescendants) {
                // O(1) - Unlock descendant
                lockedDesc.isLocked = false;
                lockedDesc.lockedBy = -1;
                
                // O(log h) - Remove from all ancestors' sets
                updateAncestorsLockedDescendants(lockedDesc, lockedDesc, false);
            }
            
            // Clear the locked descendants set
            node.lockedDescendants.clear();

            // O(1) - Lock the current node
            node.isLocked = true;
            node.lockedBy = userId;
            
            // O(log h) - Add to all ancestors' locked descendants sets
            updateAncestorsLockedDescendants(node, node, true);
            
            return true;
        }

        // O(log h) - Check for locked ancestor using binary lifting
        private boolean hasLockedAncestorOptimized(Node node) {
            int nodeIndex = nodeNames.indexOf(node.name);
            int currentDepth = node.depth;
            
            // Try to find locked ancestor at each power of 2
            for(int j = 0; j <= maxLog && currentDepth > 0; j++) {
                Node ancestor = this.ancestor[nodeIndex][j];
                if(ancestor != null && ancestor.isLocked) {
                    return true;
                }
                // Move up 2^j levels
                currentDepth -= (1 << j);
                if(currentDepth > 0) {
                    nodeIndex = nodeNames.indexOf(ancestor.name);
                }
            }
            return false;
        }

        // O(log h) - Update all ancestors' locked descendants sets using binary lifting
        private void updateAncestorsLockedDescendants(Node node, Node lockedNode, boolean isAdded) {
            int nodeIndex = nodeNames.indexOf(node.name);
            int currentDepth = node.depth;
            
            // Update ancestors at each level using binary lifting
            for(int j = 0; j <= maxLog && currentDepth > 0; j++) {
                Node ancestor = this.ancestor[nodeIndex][j];
                if(ancestor != null) {
                    if(isAdded) {
                        ancestor.lockedDescendants.add(lockedNode);
                    } else {
                        ancestor.lockedDescendants.remove(lockedNode);
                    }
                    // Move up 2^j levels
                    currentDepth -= (1 << j);
                    if(currentDepth > 0) {
                        nodeIndex = nodeNames.indexOf(ancestor.name);
                    }
                }
            }
        }

        // O(n_subtree) - Collect locked descendants by user ID (DFS)
        private boolean collectingLockedDescendantByUidOptimized(Node node, int userId) {
            // Check current node
            if(node.isLocked && node.lockedBy != userId) {
                return false;
            }
            
            // Check all children recursively
            for(Node child : node.children) {
                if(!collectingLockedDescendantByUidOptimized(child, userId)) {
                    return false;
                }
            }
            return true;
        }

        // Legacy methods for comparison (O(h) complexity)
        private boolean hasLockedAncestor(Node node){
            Node current = node.parent;
            while(current != null){
                if(current.isLocked) return true;
                current = current.parent;
            }
            return false;
        }

        private boolean hasLockedDescendant(Node node){
            for(Node child : node.children){
                if(child.isLocked || hasLockedDescendant(child)) return true;
            }
            return false;
        }

        private boolean collectingLockedDescendantByUid(Node node, int userId, List<Node> list){
            for(Node child : node.children){
                if(!collectingLockedDescendantByUid(child, userId, list)) return false;
            }
            if(node.isLocked){
                if(node.lockedBy != userId) return false;
                list.add(node);
            }
            return true;
        }
    }

    public static void main(String args[] ) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        int n = Integer.parseInt(br.readLine());
        int m = Integer.parseInt(br.readLine());
        int q = Integer.parseInt(br.readLine());

        List<String> nodeNames = new ArrayList<>();

        for(int i = 0; i < n; i++){
            nodeNames.add(br.readLine().trim());
        }

        TreeOfSpaceMySolution tree = new TreeOfSpaceMySolution(nodeNames, m);

        for(int i = 0; i < q; i++){
            String[] parts = br.readLine().trim().split("\\s+");
            int type = Integer.parseInt(parts[0]);
            String name = parts[1];
            int userId = Integer.parseInt(parts[2]);

            boolean result = switch(type){
                case 1 -> tree.lock(name, userId);
                case 2 -> tree.unlock(name, userId);
                case 3 -> tree.upgrade(name, userId);
                default -> false;
            };

            System.out.println(result);
        }
    }
}