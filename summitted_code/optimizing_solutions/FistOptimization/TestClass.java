package summitted_code.optimizing_solutions.FistOptimization;

import java.io.*;
import java.util.*;

class Node {
    String name;
    Node parent;
    List<Node> children;
    boolean isLocked;
    int lockedBy;
    Set<Node> lockedDescendants;

    public Node(String name){
        this.name = name;
        this.children = new ArrayList<>();
        this.isLocked = false;
        this.lockedBy = -1;
        this.lockedDescendants = new HashSet<>();
    }
}

class TestClass {
    static class TreeOfSpaceMySolution{

        private Map<String, Node> nodeMap = new HashMap<>();

        public TreeOfSpaceMySolution(List<String> nodeNames, int m){
            
            for(String name : nodeNames){
                nodeMap.put(name, new Node(name));
            }

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
         * Complexity notation used:
         * - h: height from the node to the root
         * - n_subtree: number of nodes in the node's subtree
         * - k: number of locked descendants of the node
         * Assumptions: HashMap/HashSet ops are average-case O(1).
         */

        public boolean lock(String name, int userId){
            // O(1)
            Node node = nodeMap.get(name);
            // node.isLocked: O(1)
            // hasLockedAncestor(node): O(h)
            // node.lockedDescendants.size() > 0: O(1)
            // total condition: O(h)
            if(node.isLocked || hasLockedAncestor(node) || node.lockedDescendants.size() > 0) return false;

            // O(1)
            node.isLocked = true;
            // O(1)
            node.lockedBy = userId;
            // Walks up ancestors; each add to HashSet is O(1) avg ⇒ O(h)
            updateDescendantsOfAncestor(node, node, true);
            // O(1)
            return true;
        }

        private boolean updateDescendantsOfAncestor(Node node, Node lockedNode, boolean isAdded){
            Node current = node.parent;
            while(current != null){
                if(isAdded) current.lockedDescendants.add(lockedNode);
                else current.lockedDescendants.remove(lockedNode);
                current = current.parent;
            }
            return true;
        }

        public boolean unlock(String name, int userId){
            // O(1)
            Node node = nodeMap.get(name);
            // node.isLocked: O(1), node.lockedBy != userId: O(1) ⇒ O(1)
            if(!node.isLocked || node.lockedBy != userId) return false;

            // O(1)
            node.isLocked = false;
            // O(1)
            node.lockedBy = -1;
            // Remove from all ancestors' sets ⇒ O(h)
            updateDescendantsOfAncestor(node, node, false);
            // O(1)
            return true;
        }

        public boolean upgrade(String name, int userId){
            // O(1)
            Node node = nodeMap.get(name);
            // node == null: O(1), node.isLocked: O(1), hasLockedAncestor: O(h) ⇒ O(h)
            if(node == null || node.isLocked || hasLockedAncestor(node)) return false;

            // node.lockedDescendants.isEmpty(): O(1)
            // collectingLockedDescendantByUid(node, userId): DFS over subtree ⇒ O(n_subtree)
            if(node.lockedDescendants.isEmpty() || !collectingLockedDescendantByUid(node, userId)) return false;


            // unlock the locked descendants
            // Loop over k locked descendants ⇒ k iterations
            for(Node n : node.lockedDescendants){
                // O(1)
                n.isLocked = false;
                // O(1)
                n.lockedBy = -1;
                // Remove from all ancestors' sets ⇒ O(h) per locked descendant
                updateDescendantsOfAncestor(n, n, false);
            }

            // O(1)
            node.isLocked = true;
            // O(1)
            node.lockedBy = userId;
            // Add to all ancestors' sets ⇒ O(h)
            updateDescendantsOfAncestor(node, node, true);
            // Total worst-case for upgrade: O(n_subtree + k·h)
            return true;
        }

        private boolean hasLockedAncestor(Node node){
            Node current = node.parent;
            while(current != null){
                if(current.isLocked) return true;
                current = current.parent;
            }
            return false;
        }


        // private boolean hasLockedDescendant(Node node){
        //     for(Node child : node.children){
        //         if(child.isLocked || hasLockedDescendant(child)) return true;
        //     }
        //     return false;
        // }

        private boolean collectingLockedDescendantByUid(Node node, int userId){
            if(node.isLocked && node.lockedBy != userId) return false;
            for(Node child : node.children){
                if(!collectingLockedDescendantByUid(child, userId)) return false;
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