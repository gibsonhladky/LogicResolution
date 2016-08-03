package resolution;

import processing.core.PApplet;
import processing.data.XML;

// Definitions for naming conventions used.
// atom: a single named proposition with no children independent of whether
// it is negated
// literal: either an atom-node containing a name, or a not-node with that
// atom as a child
// clause: an or-node, all the children of which are literals
// set: an and-node, all the children of which are clauses (disjunctions)
// Tautology: a clause that is always true
public class Resolution extends DrawableTree {
	public Resolution(PApplet p, XML tree) {
		super(p);
		this.tree = tree;
		dirtyTree = true;
	}

	// Replaces all biconditions with truth preserving conjunctions of
	// conditions.
	public void eliminateBiconditions() {
		eliminateBiconditionsRecursive(tree);
		dirtyTree = true; // View changes in Processing window
	}

	// Recursively searches through the logic tree to replace and biconditionals
	// with truth preserving conjunctions of conditions.
	private void eliminateBiconditionsRecursive(XML node) {
		for (XML child : node.getChildren()) {
			// Recurse down tree
			eliminateBiconditionsRecursive(child);
			if (isBicondition(child)) {
				decomposeBicondition(child);
			}
		}
	}

	private boolean isBicondition(XML node) {
		return node.getName().equals("bicondition");
	}

	// From: A <=> B To: (A => B) && (B => A)
	private void decomposeBicondition(XML bicondition) {
		bicondition.addChild(getForwardConditionFrom(bicondition));
		bicondition.addChild(getBackwardConditionFrom(bicondition));

		bicondition.removeChild(bicondition.getChild(1));
		bicondition.removeChild(bicondition.getChild(0));

		bicondition.setName("and");
	}

	// Returns A => B from A <=> B
	private XML getForwardConditionFrom(XML bicondition) {
		XML forwardCondition = new XML("condition");
		forwardCondition.addChild(bicondition.getChild(0));
		forwardCondition.addChild(bicondition.getChild(1));
		return forwardCondition;
	}

	// Returns B => A from A <=> B
	private XML getBackwardConditionFrom(XML bicondition) {
		XML backwardCondition = new XML("condition");
		backwardCondition.addChild(bicondition.getChild(1));
		backwardCondition.addChild(bicondition.getChild(0));
		return backwardCondition;
	}

	// Replace all conditions with truth preserving disjunctions.
	public void eliminateConditions() {
		eliminateConditionsRecursive(tree);
		dirtyTree = true; // View changes in Processing window
	}

	// Recursively replace conditions with truth preserving disjunctions
	private void eliminateConditionsRecursive(XML node) {
		for (XML child : node.getChildren()) {
			eliminateConditionsRecursive(child);
			if (isCondition(child)) {
				decomposeCondition(child);
			}
		}
	}

	private boolean isCondition(XML node) {
		return node.getName().equals("condition");
	}

	// Returns (A => B) from (B || !A)
	private void decomposeCondition(XML condition) {
		// Replace A with (!A)
		XML not = new XML("not");
		not.addChild(condition.getChild(0));

		condition.addChild(not);
		condition.removeChild(condition.getChild(0));

		condition.setName("or");
	}

	// Move negations in a truth preserving way to apply only to literals.
	public void moveAllNegationsInwards() {
		moveAllNegationsInwardsRecursive(tree);
		dirtyTree = true; // View changes in Processing window
	}

	// Recursively move negations in a truth preserving way to apply only to
	// literals
	private void moveAllNegationsInwardsRecursive(XML node) {
		for (XML child : node.getChildren()) {
			if (isNot(child)) {
				// Keep any easy access variable to not's child
				XML grandchild = child.getChild(0);
				if (isTerm(grandchild)) {
					continue;
				}
				replaceChildWith(child, negate(grandchild));
				// Recurse on this node again, since the structure has changed
				moveAllNegationsInwardsRecursive(node);
			}
			else {
				moveAllNegationsInwardsRecursive(child);
			}
		}
	}

	private void replaceChildWith(XML child, XML replacement) {
		child.getParent().addChild(replacement);
		child.getParent().removeChild(child);
	}

	// TODO: eliminate duplication for deMorgan's law
	private XML negate(XML node) {
		if (isNot(node)) {
			return node.getChild(0);
		}
		else if (isAnd(node)) {
			XML newNode = new XML("or");
			XML not1 = new XML("not");
			XML not2 = new XML("not");

			not1.addChild(node.getChild(0));
			not2.addChild(node.getChild(1));
			newNode.addChild(not1);
			newNode.addChild(not2);

			return newNode;
		}
		else if (isOr(node)) {
			XML newNode = new XML("and");
			XML not1 = new XML("not");
			XML not2 = new XML("not");

			not1.addChild(node.getChild(0));
			not2.addChild(node.getChild(1));
			newNode.addChild(not1);
			newNode.addChild(not2);

			return newNode;
		}
		return null;
	}

	// Move negations in a truth preserving way to apply only to literals.
	public void distributeOrsOverAnds() {
		distributeOrsoverAndsRecursive(tree);
		dirtyTree = true; // View changes in Processing window
	}

	// Recursively move negations in a truth preserving way to apply only to
	// literals
	private void distributeOrsoverAndsRecursive(XML node) {
		for (XML curr : node.getChildren()) {
			if (isDistributableOver(curr)) {
				distributeOverOr(curr);
				distributeOrsoverAndsRecursive(node); // Recurse on new
														// structure
			}
			distributeOrsoverAndsRecursive(curr);
		}
	}

	private boolean isDistributableOver(XML node) {
		if (isOr(node)) {
			XML left = node.getChild(0);
			XML right = node.getChild(1);
			return isAnd(left) || isAnd(right);
		}
		return false;
	}

	// Distributes an AND over the OR node, restructuring the tree
	// in a truth preserving manner, and replacing the OR node with AND.
	private void distributeOverOr(XML or) {
		XML left = or.getChild(0);
		XML right = or.getChild(1);
		XML newLeft;
		XML newRight;
		if (isAnd(left)) {
			// curr = (X&&Y)||Z will become (X||Z)&&(Y||Z)
			newLeft = createOr(left.getChild(0), right);
			newRight = createOr(left.getChild(1), right);
		}
		else {
			// curr = X||(Y&&Z) will become (X||Y)&&(X||Z)
			newLeft = createOr(left, right.getChild(0));
			newRight = createOr(left, right.getChild(1));
		}
		replaceChildWith(left, newLeft);
		replaceChildWith(right, newRight);
		or.setName("and");
	}

	private XML createOr(XML left, XML right) {
		XML or = new XML("or");
		or.addChild(left);
		or.addChild(right);
		return or;
	}

	// Cleans up logic in tree in preparation for Resolution:
	// 1) Converts nested binary ands and ors into n-ary operators so
	// there is a single and-node child of the root logic-node, all of
	// the children of this and-node are or-nodes, and all of the
	// children of these or-nodes are literals: either atomic or negated
	// 2) Removes redundant literals from every clause, and then remove
	// redundant clauses from the tree.
	// 3) Removes any clauses that are always true (tautologies)
	// from the tree to help speed up resolution.
	public void collapse() {
		unnestLogic(tree);
		guaranteeFormat();
		Set set = new Set(tree.getChild(0));
		replaceChildWith(tree.getChild(0), set.toXML());
		dirtyTree = true;
	}
	
	// Compresses the logic to a single AND with multiple OR children.
	// Each OR has only literal children.
	// Assumes that the nodes have been decomposed correctly
	private void unnestLogic(XML node) {
		while(hasChildWithMatchingName(node)){
			compressNode(node);
		}
		for(XML child : node.getChildren()) {
			unnestLogic(child);
		}
	}
	
	private boolean hasChildWithMatchingName(XML node) {
		for(XML child : node.getChildren()) {
			if(child.getName().equals(node.getName())) {
				return true;
			}
		}
		return false;
	}
	
	private void compressNode(XML node) {
		for (XML child : node.getChildren()) {
			if (child.getName().equals(node.getName())) {
				replaceChildWithGrandchildren(child);
			}
		}
	}
	
	private void replaceChildWithGrandchildren(XML child) {
		XML parent = child.getParent();
		for (XML grandchild : child.getChildren()) {
			parent.addChild(grandchild);
		}
		parent.removeChild(child);
	}
	
	// Guarantees a format of root = logic, with one AND child
	// that has multiple OR children
	private void guaranteeFormat() {
		ensureFirstNodeIsAnd();
		XML and = tree.getChild(0);
		ensureAllChildrenAre("or", and);
	}
	
	private void ensureFirstNodeIsAnd() {
		XML firstNode = tree.getChild(0);
		if (!firstNode.getName().equals("and")) {
			replaceChildWith(firstNode, wrapWith("and", firstNode));
		}
	}
	
	private void ensureAllChildrenAre(String childType, XML node) {
		for(XML child : node.getChildren()) {
			if (!child.getName().equals(childType)) {
				replaceChildWith(child, wrapWith(childType, child));
			}
		}
	}
	
	private XML wrapWith(String wrapperType, XML node) {
		XML wrapper = new XML(wrapperType);
		wrapper.addChild(node);
		return wrapper;
	}

	// Implements resolution on the logic in tree. New resolvents
	// are added as children to the only and-node in tree. This
	// method returns true when a conflict is found, otherwise it
	// only returns false after exploring all possible resolvents.
	public boolean applyResolution() {
		Set set = new Set(tree.getChild(0));
		set.resolve();
		replaceChildWith(tree.getChild(0), set.toXML());
		dirtyTree = true;
		return set.containsConflict();
	}

	private boolean isTerm(XML node) {
		return !( isNot(node) || isOr(node) || isAnd(node) || isCondition(node) || isBicondition(node) );
	}

	private boolean isNot(XML node) {
		return node.getName().equals("not");
	}

	private boolean isOr(XML node) {
		return node.getName().equals("or");
	}

	private boolean isAnd(XML node) {
		return node.getName().equals("and");
	}
}
