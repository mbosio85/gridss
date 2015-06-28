package au.edu.wehi.idsv.debruijn.positional;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;

import com.google.common.collect.RangeSet;


public class KmerPathNodePath extends KmerPathNodeBasePath {
	private ArrayDeque<TraversalNode> nodepath = new ArrayDeque<TraversalNode>();
	private ArrayDeque<KmerPathNode> path = new ArrayDeque<KmerPathNode>();
	private ArrayDeque<Iterator<TraversalNode>> nextPath = new ArrayDeque<Iterator<TraversalNode>>();
	public KmerPathNodePath(KmerPathSubnode node, boolean traverseForward, int maxPathLength) {
		super(node, maxPathLength, traverseForward);
		dfsPush(rootNode());
	}
	public int pathLength() {
		return (traversingForward() ? nodepath.getLast() : nodepath.getFirst()).pathLength();
	}
	public int pathWeight() {
		int weight = 0;
		for (KmerPathNode n : path) {
			weight += n.weight();
		}
		return weight;
	}
	protected KmerPathNode headPath() {
		return traversingForward() ? path.getLast() : path.getFirst();
	}
	protected TraversalNode headNode() {
		return traversingForward() ? nodepath.getLast() : nodepath.getFirst();
	}
	protected Iterator<TraversalNode> headNext() {
		return traversingForward() ? nextPath.getLast() : nextPath.getFirst();
	}
	/**
	 * Traverse to the next child of this node
	 * @return
	 */
	public boolean dfsNextChild() {
		if (headNext().hasNext()) {
			dfsPush(headNext().next());
			return true;
		}
		return false;
	}
	private void dfsPush(TraversalNode next) {
		if (traversingForward()) {
			nodepath.addLast(next);
			path.addLast(next.node().node());
			nextPath.addLast(next.iterator());
		} else {
			nodepath.addFirst(next);
			path.addFirst(next.node().node());
			nextPath.addFirst(next.iterator());
		}
	}
	/**
	 * Resets traversal of all child nodes to an untraversed state
	 */
	public void dfsResetChildTraversal() {
		TraversalNode node = headNode();
		dfsPopUnchecked();
		dfsPush(node);
	}
	/**
	 * Stop any further child traversal of this node and remove it from the path
	 */
	public void dfsPop() {
		if (nodepath.size() == 1) throw new IllegalStateException("Cannot remove root node from traversal path");
		dfsPopUnchecked();
	}
	private void dfsPopUnchecked() {
		if (traversingForward()) {
			nodepath.removeLast();
			path.removeLast();
			nextPath.removeLast();
		} else {
			nodepath.removeFirst();
			path.removeFirst();
			nextPath.removeFirst();
		}
	}
	public Collection<KmerPathNode> currentPath() {
		return path;
	}
	public RangeSet<Integer> terminalRanges() {
		return headNode().terminalRanges();
	}
	public RangeSet<Integer> terminalLeafRanges() {
		return headNode().terminalLeafAnchorRanges();
	}
	public void greedyTraverse(boolean allowReference, boolean allowNonReference) {
		TraversalNode best;
		do {
			best = null;
			Iterator<TraversalNode> it = headNext();
			while (it.hasNext()) {
				TraversalNode n = it.next();
				boolean isRef = n.node().node().isReference();
				if ((isRef && allowReference) || (!isRef && allowNonReference)) {
					if (best == null || n.node().weight() > best.node().weight()) {
						best = n;
					}
				}
			}
			if (best != null) {
				dfsPush(best);
			}
		} while (best != null);
	}
	public String toString() {
		return headNode().asSubnodes().toString().replace(",", "\n");
	}
}