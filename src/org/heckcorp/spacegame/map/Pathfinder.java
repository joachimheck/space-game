package org.heckcorp.spacegame.map;

import org.heckcorp.spacegame.Unit;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Pathfinder implements Serializable {
    private static final long serialVersionUID = 1L;

    private final static class Node {
        public Node(Point point, @Nullable Node predecessor) {
            this.point = point;
            this.predecessor = predecessor;
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
        private Integer cost = null;

        /**
         * The estimated distance from this state to the goal state.
         */
        private Integer distance = null;

        public void computeDistance(Node goal) {
            distance = Calculator.distance(point, goal.point);
        }

        public int getCost() {
            return cost;
        }

        public int getDistance() {
            return distance;
        }

        public int getScore() {
            return cost + distance;
        }

        public void setCost(int cost) {
            this.cost = cost;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }
    }

    public Pathfinder(HexMap map) {
        this.map = map;
    }

    public List<Hex> findPath(Unit unit, Hex goalHex) {
        Node goal = new Node(goalHex.getPosition(), null);
        Node start = new Node(unit.getHex().getPosition(), null);

        start.setCost(0);
        start.computeDistance(goal);

        Set<Node> openNodes = new HashSet<>();
        Set<Node> closedNodes = new HashSet<>();

        // Put the start state (hex) on the open list.
        openNodes.add(start);

        Node current = null;
        while (!openNodes.isEmpty()) {
            current = getLowestScoreNode(openNodes);

            if (current.point.equals(goal.point)) {
                break;
            } else {
                // Generate all the successors.
                Set<Node> successors = generateSuccessors(current);

                for (Node successor : successors) {
                    // TODO: fix cost computation.
                    successor.setCost(current.getCost() + 1);
                    Node alreadyOpen = findNode(openNodes, successor);
                    Node alreadyClosed = findNode(closedNodes, successor);

                    if ((alreadyOpen == null ||
                        alreadyOpen.getCost() > successor.getCost()) &&
                        (alreadyClosed == null ||
                            alreadyClosed.getCost() > successor.getCost()))
                    {
                        openNodes.remove(alreadyOpen);
                        closedNodes.remove(alreadyClosed);
                        successor.computeDistance(goal);
                        openNodes.add(successor);
                    }
                }

                // Move the current node to the closed list.
                openNodes.remove(current);
                closedNodes.add(current);
            }
        }

        // Process current to get the path.
        if (!current.point.equals(goal.point)) {
            return Collections.emptyList();
        }

        return createPath(start, current);
    }

    private List<Hex> createPath(Node start, Node end) {
        List<Hex> path = new ArrayList<>();

        Node current = end;
        while (current != null && current != start) {
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
    private Node findNode(Set<Node> nodes, Node target) {
        Node found = null;

        for (Node node : nodes) {
            if (node.point.equals(target.point)) {
                found = node;
                break;
            }
        }

        return found;
    }

    private Node getLowestScoreNode(Set<Node> nodes) {
        int lowestScore = Integer.MAX_VALUE;
        Node best = null;

        for (Node node : nodes) {
            if (node.getScore() < lowestScore) {
                best = node;
                lowestScore = node.getScore();
            }
        }

       return best;
    }

    private final HexMap map;
}
