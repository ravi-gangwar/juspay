package threadsafe.PossibleSolution;


import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*; // TODO: Added for ReentrantLock

class Node {
    String name;
    Node parent;
    List<Node> children;
    boolean isLocked;
    int lockedBy;
    Set<Node> lockedDescendants;
    Map<Integer, Integer> descendantLockHistoryByUid;

    // TODO: Add per-node lock
    final ReentrantLock lock = new ReentrantLock(true); // we can use fair lock for fair scheduling (FCFS)

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

        // ===================== THREAD SAFE LOCK =====================
        public boolean lock(String name, int userId){
            Node node = nodeMap.get(name);

            // TODO: Step 1: Collect path root → node
            List<Node> path = getPathToRoot(node);

            // TODO: Step 2: Lock all nodes in order (to prevent deadlock)
            for(Node n : path) n.lock.lock();

            try {
                // Step 3a: Check if node is already locked
                if(node.isLocked) return false;

                // Step 3b: Check locked ancestors
                if(hasLockedAncestor(node)) return false;

                // Step 3c: Check locked descendants
                if(!node.lockedDescendants.isEmpty()) return false;

                // Step 3d: Lock node
                node.isLocked = true;
                node.lockedBy = userId;

                // Step 3e: Update ancestors’ metadata
                updateDescendantsOfAncestor(node, node, true);

                return true;
            } finally {
                // TODO: Step 4: Release locks in reverse order
                unlockPath(path);
            }
        }

        // ===================== THREAD SAFE UNLOCK =====================
        public boolean unlock(String name, int userId){
            Node node = nodeMap.get(name);

            List<Node> path = getPathToRoot(node);
            for(Node n : path) n.lock.lock();

            try {
                if(!node.isLocked || node.lockedBy != userId) return false;

                node.isLocked = false;
                node.lockedBy = -1;

                updateDescendantsOfAncestor(node, node, false);

                return true;
            } finally {
                unlockPath(path);
            }
        }

        // ===================== THREAD SAFE UPGRADE =====================
        public boolean upgrade(String name, int userId){
            Node node = nodeMap.get(name);

            List<Node> path = getPathToRoot(node);
            for(Node n : path) n.lock.lock();

            try {
                if(node == null || node.isLocked) return false;
                if(hasLockedAncestor(node)) return false;
                if(node.lockedDescendants.isEmpty()) return false;
                if(node.descendantLockHistoryByUid.size() != 1) return false;
                if(node.descendantLockHistoryByUid.getOrDefault(userId, 0) == 0) return false;

                // Unlock all descendants
                for(Node n : new HashSet<>(node.lockedDescendants)) {
                    n.isLocked = false; // TODO: raise contention
                    n.lockedBy = -1; // TODO: raise contention
                    n.lockedDescendants.clear();
                    n.descendantLockHistoryByUid.clear();
                    updateDescendantsOfAncestor(n, n, false);
                }

                // Lock current node
                node.isLocked = true;
                node.lockedBy = userId;
                node.descendantLockHistoryByUid.clear();
                node.lockedDescendants.clear();
                updateDescendantsOfAncestor(node, node, true);

                return true;
            } finally {
                unlockPath(path);
            }
        }

        // ===================== HELPERS =====================

        // TODO: Utility to collect path root → node
        private List<Node> getPathToRoot(Node node) {
            List<Node> path = new ArrayList<>();
            Node cur = node;
            while(cur != null) {
                path.add(cur);
                cur = cur.parent;
            }
            Collections.reverse(path);
            return path;
        }

        // TODO: Utility to release locks
        private void unlockPath(List<Node> path) {
            for(int i = path.size() - 1; i >= 0; i--) {
                path.get(i).lock.unlock();
            }
        }

        private boolean updateDescendantsOfAncestor(Node node, Node lockedNode, boolean isAdded){
            Node current = node.parent;
            while(current != null){
                if(isAdded) current.lockedDescendants.add(lockedNode);
                else current.lockedDescendants.remove(lockedNode);

                if(isAdded) {
                    current.descendantLockHistoryByUid.put(
                        lockedNode.lockedBy,
                        current.descendantLockHistoryByUid.getOrDefault(lockedNode.lockedBy, 0) + 1
                    );
                } else {
                    int count = current.descendantLockHistoryByUid.getOrDefault(lockedNode.lockedBy, 0);
                    if(count <= 1) current.descendantLockHistoryByUid.remove(lockedNode.lockedBy);
                    else current.descendantLockHistoryByUid.put(lockedNode.lockedBy, count - 1);
                }
                current = current.parent;
            }
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