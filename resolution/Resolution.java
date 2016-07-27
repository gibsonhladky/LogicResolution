package resolution;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.data.XML;

public class Resolution extends DrawableTree
{
	public Resolution(PApplet p, XML tree) 
	{ 
		super(p); 
		this.tree = tree; 
		dirtyTree = true;
	}
		
	
	// Replaces all biconditions with truth preserving conjunctions of conditions. 
	public void eliminateBiconditions()
	{
		eliminateBiconditionsRecursive(tree);
		dirtyTree = true; // View changes in Processing window
	}	
	
	
	// Recursively searches through the logic tree to replace and biconditionals
	// with truth preserving conjunctions of conditions.
	private void eliminateBiconditionsRecursive(XML node)
	{
		for(XML child : node.getChildren()) {
			// Recurse down tree
			eliminateBiconditionsRecursive(child);
			if(isBicondition(child)) {
				decomposeBicondition(child);
			}
		}
	}
	
	private boolean isBicondition(XML node) {
		return node.getName().equals("bicondition");
	}
	
	// From: A <=> B  To: (A => B) && (B => A)
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
	public void eliminateConditions()
	{
		eliminateConditionsRecursive(tree);
		dirtyTree = true; // View changes in Processing window
	}
	
	
	// Recursively replace conditions with truth preserving disjunctions
	private void eliminateConditionsRecursive(XML node)
	{
		for(XML child : node.getChildren()) {
			eliminateConditionsRecursive(child);
			if(isCondition(child)) {
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
	public void moveAllNegationsInwards()
	{
		moveAllNegationsInwardsRecursive(tree);
		dirtyTree = true; // View changes in Processing window
	}
	
	
	// Recursively move negations in a truth preserving way to apply only to literals
	private void moveAllNegationsInwardsRecursive(XML node) {
		for(XML child : node.getChildren()) {
			if(isNot(child)) {
				// Keep any easy access variable to not's child
				XML grandchild = child.getChild(0);
				if(isTerm(grandchild)) {
					continue;
				}
				replace_With(child, negate(grandchild));
				// Recurse on this node again, since the structure has changed
				moveAllNegationsInwardsRecursive(node);
			}
			else {
				moveAllNegationsInwardsRecursive(child);
			}
		}
	}
	
	private void replace_With(XML child, XML replacement) {
		child.getParent().addChild(replacement);
		child.getParent().removeChild(child);
	}
	
	// TODO: eliminate duplication for deMorgan's law
	private XML negate(XML node) {
		if(isNot(node)) {
			return node.getChild(0);
		}
		else if(isAnd(node)) {
			XML newNode = new XML("or");
			XML not1 = new XML("not");
			XML not2 = new XML("not");
			
			not1.addChild(node.getChild(0));
			not2.addChild(node.getChild(1));
			newNode.addChild(not1);
			newNode.addChild(not2);
			
			return newNode;
		}
		else if(isOr(node)) {
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
	
	private boolean isTerm(XML node) {
		return !(isNot(node) || isOr(node) || isAnd(node) || isCondition(node) || isBicondition(node));
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
	
	
	// Move negations in a truth preserving way to apply only to literals.
	public void distributeOrsOverAnds()
	{
		distributeOrsoverAndsRecursive(tree);
		dirtyTree = true; // View changes in Processing window
	}
	
	
	// Recursively move negations in a truth preserving way to apply only to literals
	private void distributeOrsoverAndsRecursive(XML node) {	
		for(XML curr : node.getChildren()) {
			if(isDistributableOver(curr)) {
				distributeOverOr(curr);
				distributeOrsoverAndsRecursive(node); // Recurse on new structure
			}
			distributeOrsoverAndsRecursive(curr);
		}
	}
	
	private boolean isDistributableOver(XML node) {
		if(isOr(node)) {
			XML left = node.getChild(0);
			XML right = node.getChild(1);
			return isAnd(left) || isAnd(right);
		}
		return false;
	}
	
	private void distributeOverOr(XML or) {
		XML left = or.getChild(0);
		XML right = or.getChild(1);
		if(isAnd(left)) {
			// curr = (X&&Y)||Z will become (X||Z)&&(Y||Z)
			XML newLeft = createOr(left.getChild(0), right);
			XML newRight = createOr(left.getChild(1), right);

			replace_With(left, newLeft);
			replace_With(right, newRight);
			or.setName("and");
		}
		else if(isAnd(right)) {
			// curr = X||(Y&&Z) will become (X||Y)&&(X||Z)
			XML newLeft = createOr(left, right.getChild(0));
			XML newRight = createOr(left, right.getChild(1));
			
			replace_With(left, newLeft);
			replace_With(right, newRight);
			or.setName("and");
		}
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
	public void collapse()
	{
		unnestAndsOrs(tree.getChild(0));
		
		guaranteeFormat();
		
		removeRedundancy(tree.getChild(0));
		
		removeTautologies(tree.getChild(0));
		
		dirtyTree = true;
	}	
	
	
	// Converts binary AND/OR nodes to multi-child nodes.
	// Assumes ORs have been distributed over ANDs for proper function
	private void unnestAndsOrs(XML node) {
		XML child;
		boolean changed = false;
		if(node.getName().equals("and")) {
			// Compress AND nodes
			for(int i = 0; i < node.getChildCount(); i++) {
				child = node.getChild(i);
				if(child.getName().equals("and")) {
					// Move children from secondary AND to primary AND
					for(int j = 0; j < child.getChildCount(); j++) {
						node.addChild(child.getChild(j));
					}
					// Remove secondary AND
					node.removeChild(child);
					// Flag node as changed
					changed = true;
				}
			}
			
		}
		else if(node.getName().equals("or")) {
			// Compress OR nodes
			for(int i = 0; i < node.getChildCount(); i++) {
				child = node.getChild(i);
				if(child.getName().equals("or")) {
					// Move children from secondary OR to primary OR
					for(int j = 0; j < child.getChildCount(); j++) {
						node.addChild(child.getChild(j));
					}
					// Remove secondary OR
					node.removeChild(child);
					// Flag node as changed
					changed = true;
				}
			}
		}
		// Recurse on same node if tree has changed
		if(changed) {
			unnestAndsOrs(node);
		}
		// Recurse on all children if tree hasn't changed
		if(!changed) {
			for(int i = 0; i < node.getChildCount(); i++) {
				unnestAndsOrs(node.getChild(i));
			}
		}
	}
	
	
	// Guarantees a format of root = logic, with one AND child
	// that has multiple OR children
	private void guaranteeFormat() {
		XML firstNode = this.tree.getChild(0);
		// Ensure first node is AND
		if(!firstNode.getName().equals("and")) {
			XML and = new XML("and");
			and.addChild(firstNode);
			this.tree.removeChild(firstNode);
			this.tree.addChild(and);
			// Update firstNode after change
			firstNode = this.tree.getChild(0);
		}
		// Ensure ANDs children are all OR
		XML child;
		for(int i = 0; i < firstNode.getChildCount(); i++) {
			child = firstNode.getChild(i);
			if(!child.getName().equals("or")) {
				XML newNode = new XML("or");
				newNode.addChild(child);
				firstNode.removeChild(child);
				firstNode.addChild(newNode);
				i--;
			}
		}
	}
	
	
	// Removes redundant literals from all clauses, then removes
	// any redundant clauses
	private void removeRedundancy(XML set) {
		//Remove redundant literals from each clause:
		for(int i = 0; i < set.getChildCount(); i++) {
			removeRedundantLiterals(set.getChild(i));
		}
		// Remove redundant clauses
		XML tempClause;
		ArrayList<XML> testedClauses = new ArrayList<XML>();
		for(int i = 0; i < set.getChildCount(); i++) {
			// Remove clauses from the tree one at a time to test
			// against remaining clauses. Store the clauses in an
			// ArrayList to be added back to the tree if they are 
			// not redundant
			tempClause = set.getChild(i);
			testedClauses.add(set.getChild(i));
			set.removeChild(set.getChild(i));
			// Remove redundant clauses
			if(setContainsClause(set, tempClause)) {
				testedClauses.remove(tempClause);
			}
			i--; // Account for child deletion
		}
		// Add back all non-redundant clauses
		for(int i = 0; i < testedClauses.size(); i++) {
			set.addChild(testedClauses.get(i));
		}
	}

	
	// Removes all redundant literals from a clause
	private void removeRedundantLiterals(XML clause) {
		XML l1, l2; // Two literals
		// Compare all literals in the clause
		for(int i = 0; i < clause.getChildCount(); i++) {
			l1 = clause.getChild(i);
			for(int j = i+1; j < clause.getChildCount(); j++) {
				l2 = clause.getChild(j);
				// Remove matching literals
				if(getAtomFromLiteral(l1).equals(getAtomFromLiteral(l2))) {
					if(isLiteralNegated(l1)==isLiteralNegated(l2)) {
						clause.removeChild(l2);
						j--; // Account for node deletion
						// Do not need to decrement i because
						// j > i always.
					}
				}
			}
		}
	}
	
	
	// Removes any tautologies in a set
	private void removeTautologies(XML set) {
		XML clause;
		for(int i = 0; i < set.getChildCount(); i++) {
			clause = set.getChild(i);
			if(clauseIsTautology(clause)) {
				set.removeChild(clause);
				i--;
			}
		}
	}
	
	
	// Implements resolution on the logic in tree.  New resolvents
	// are added as children to the only and-node in tree.  This
	// method returns true when a conflict is found, otherwise it
	// only returns false after exploring all possible resolvents.
	public boolean applyResolution()
	{
		XML set = tree.getChild(0);
		XML clause1, clause2, resolvent;
		boolean updated = true;
		
		// Continue iterating until no more resolvents are found
		while(updated) {
			updated = false;
			// Compare all pairs of clauses to create new resolvents
			for(int i = 0; i < set.getChildCount(); i++) {
				clause1 = set.getChild(i);
				for(int j = i+1; j < set.getChildCount(); j++) {
					clause2 = set.getChild(j);
					// Generate a resolvent from two clauses
					resolvent = resolve(clause1, clause2);
					if(resolvent == null) {
						// Do nothing, clauses could not be resolved
					}
					else if(resolvent.getChildCount() > 0) { 
						// Add new valid non-duplicate resolvents
						if(!setContainsClause(set, resolvent)) {
								set.addChild(resolvent);
								updated = true; // Flag tree as updated
						}
					}
					else { 
						// Conflict found!
						dirtyTree = true;
						return true;
					}
				}
			}
		}
		dirtyTree = true;
		return false;
	}

	
	// Attempts to resolve two clauses and return the resulting
	// resolvent.  Removes any redundant literals from the 
	// resulting resolvent.  If there is a conflict,
	// returns an XML node with zero children.  If the two clauses cannot
	// be resolved,returns null.
	private XML resolve(XML clause1, XML clause2)
	{
		XML lit; // A literal
		XML resolvent = new XML("or");
		int inverses = 0; // A count of inverses found (i.e. A and !A)
		// Iterate over the first clause's literals
		for(int i = 0; i < clause1.getChildCount(); i++) {
			lit = clause1.getChild(i);
			// Maintain a count of inverse literals
			if(clauseContainsLiteral(clause2, getAtomFromLiteral(lit), !isLiteralNegated(lit))) {
				inverses++;
			}
			// Add non-inverse literals to the resolvent
			else {
				resolvent.addChild(lit);
			}
		}
		// If all literals are inverses, send back conflict XML node
		if(inverses == clause1.getChildCount()) {
			return new XML("conflict");
		}
		// Iterate over second clause's literals
		// This creates redundant literals in resolvent, which is handled later
		for(int i = 0; i < clause2.getChildCount(); i++) {
			lit = clause2.getChild(i);
			// Add non-conflicting literals to the resolvent
			if(!clauseContainsLiteral(clause1, getAtomFromLiteral(lit), !isLiteralNegated(lit))) {
				resolvent.addChild(lit);
			}
		}
		// NOTE: You need at least one inverse to make a resolvent
		// Return null if clauses could not be resolved
		if(inverses == 0) {
			return null;
		}
		removeRedundantLiterals(resolvent);
		return resolvent;
	}	
	
	// REQUIRED HELPERS: may be helpful to implement these before collapse(), applyResolution(), and resolve()
	// Some terminology reminders regarding the following methods:
	// atom: a single named proposition with no children independent of whether it is negated
	// literal: either an atom-node containing a name, or a not-node with that atom as a child
	// clause: an or-node, all the children of which are literals
	// set: an and-node, all the children of which are clauses (disjunctions)
		


	// Returns true when literal is negated and false otherwise.
	private boolean isLiteralNegated(XML literal) 
	{
		if(literal.getName().equals("not")) {
			return true;
		}
		else if(literal.getParent().getName().equals("not")) {
			// Implemented for robustness
			return true;
		}
		return false; 
	}


	// Returns the name of the atom in literal as a string.
	private String getAtomFromLiteral(XML literal)
	{
		if(literal.getName().equals("not")) {
			return literal.getChild(0).getName();
		}
		return literal.getName();
	}	
	

	// Returns true when the provided clause contains a literal
	// with the atomic name and negation (isNegated).  Otherwise, returns false.
	private boolean clauseContainsLiteral(XML clause, String atom, boolean isNegated)
	{
		XML child;
		// Check all children of clause for the provided literal
		for(int i = 0; i < clause.getChildCount(); i++) {
			child = clause.getChild(i);
				if(isLiteralNegated(child) == isNegated && getAtomFromLiteral(child).equals(atom)) {
					return true;
				}
		}
		return false;
	}
	
	
	// Returns true when the set contains a clause with the
	// same set of literals as the clause parameter.  Otherwise, returns false.
	private boolean setContainsClause(XML set, XML clause)
	{
		boolean negated;
		boolean isClause = true;
		// Check all children of set
		for(int i = 0; i < set.getChildCount(); i++) {
			XML test_clause = set.getChild(i);
			isClause = true;
			// Test for clauses with same and extra literals
			if(clause.getChildCount()!=test_clause.getChildCount()) {
				isClause = false;
			}
			// Check for all matching children between clauses
			for(int j = 0; j < clause.getChildCount(); j++) {
				negated = isLiteralNegated(clause.getChild(j));
				if(!clauseContainsLiteral(test_clause, getAtomFromLiteral(clause.getChild(j)), negated)) {
					isClause = false;
				}
			}
			if(isClause) {
				return true;
			}
		}
		return false;
	}
	
	
	// Returns true when this clause contains a literal,
	// along with the negated form of that same literal.
	// Otherwise, returns false.
	private boolean clauseIsTautology(XML clause)
	{
		XML literal;
		// Iterate over all children of clause
		for(int i = 0; i < clause.getChildCount(); i++) {
			literal = clause.getChild(i);
			if(clauseContainsLiteral(clause, getAtomFromLiteral(literal), !isLiteralNegated(literal))) {
				return true;
			}
		}
		
		return false;
	}	
	
}
