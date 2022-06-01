package org.heckcorp.spacegame.map;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class Pathfinder implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final static class Node {
        public Node(Point point, Node predecessor) {
            this.point = point;
            this.predecessor = predecessor;
        }

        private Node(Point point) {
            this.point = point;
            this.predecessor = this;
        }

        /**
         * The state represented by this node.
         */
        public final Point point;

        /**
         * The predecessor to this node along a path.
         */
        public final Node predecessor;

        /**
         * The cost to reach this state from the initial state.
         */
        private int cost = Integer.MAX_VALUE;

        /**
         * The estimated distance from this state to the goal state.
         */
        private int distance = Integer.MAX_VALUE;

        public void computeDistance(Node goal) {
            distance = Calculator.distance(point, goal.point);
        }

        public int getCost() {
            return cost;
        }

        public int getScore() {
            return cost + distance;
        }

        public void setCost(int cost) {
            this.cost = cost;
        }

        public static Node INVALID = new Node(new Point(-1, -1));
    }

    public Pathfinder(HexMap map) {
        this.map = map;
    }

    public List<Hex> findPath(Hex startHex, Hex goalHex) {
        Node goal = new Node(goalHex.getPosition(), Node.INVALID);
        Node start = new Node(startHex.getPosition(), Node.INVALID);

        start.setCost(0);
        start.computeDistance(goal);

        Set<Node> openNodes = new HashSet<>();
        Set<Node> closedNodes = new HashSet<>();

        // Put the start state (hex) on the open list.
        openNodes.add(start);

        Optional<Node> current = Optional.empty();
        while (!openNodes.isEmpty()) {
            current = getLowestScoreNode(openNodes);

            if (current.isEmpty() || current.get().point.equals(goal.point)) {
                break;
            } else {
                Node currentNode = current.get();
                // Generate all the successors.
                Set<Node> successors = generateSuccessors(currentNode);

                for (Node successor : successors) {
                    // TODO: fix cost computation.
                    successor.setCost(currentNode.getCost() + 1);
                    Optional<Node> alreadyOpen = findNode(openNodes, successor);
                    Optional<Node> alreadyClosed = findNode(closedNodes, successor);

                    if ((alreadyOpen.isEmpty() || alreadyOpen.get().getCost() > successor.getCost())
                            && (alreadyClosed.isEmpty() || alreadyClosed.get().getCost() > successor.getCost())) {
                        alreadyOpen.ifPresent(openNodes::remove);
                        alreadyClosed.ifPresent(closedNodes::remove);
                        successor.computeDistance(goal);
                        openNodes.add(successor);
                    }
                }

                // Move the current node to the closed list.
                openNodes.remove(currentNode);
                closedNodes.add(currentNode);
            }
        }

        // Process current to get the path.
        if (current.isEmpty() || !current.get().point.equals(goal.point)) {
            return Collections.emptyList();
        }

        return createPath(start, current.get());
    }

    private List<Hex> createPath(Node start, Node end) {
        List<Hex> path = new ArrayList<>();

        Node current = end;
        while (current != start) {
            path.add(0, map.getHex(current.point));
            current = current.predecessor;
        }

        return path;
    }

    private Set<Node> generateSuccessors(Node node) {
        Hex startHex = map.getHex(node.point);
        List<Hex> hexes = map.getHexesInRange(startHex, 1);

        assert !hexes.contains(startHex);

        Set<Node> successors = new HashSet<>();
        for (Hex hex : hexes) {
            successors.add(new Node(hex.getPosition(), node));
        }

        return successors;
    }

    /**
     * Finds a node with the same state as the specified node
     * in the set of nodes and returns it if it is present.
     */
    private Optional<Node> findNode(Set<Node> nodes, Node target) {
        return nodes.stream().filter(n -> n.point.equals(target.point)).findFirst();
    }

    private Optional<Node> getLowestScoreNode(Set<Node> nodes) {
        return nodes.stream().min(Comparator.comparingInt(Node::getScore));
    }

    private final HexMap map;
}
