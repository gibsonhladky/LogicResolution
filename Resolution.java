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
	public void eliminateBiconditionsRecursive(XML node)
	{
		XML[] children = node.getChildren();
		// Search for a biconditional child
		// Base case: leaf node (no children)
		for(int i = 0; i < children.length; i++) {
			XML curr = children[i];
			// Recurse down tree
			eliminateBiconditionsRecursive(curr);
			// Replace bicondition with conjunction of conditions
			if(curr.getName().equals("bicondition")) {
				// curr = A<=>B will become (A=>B)&&(B=>A) //
				// Create truth preserving conditions:
				XML left = new XML("condition");
				XML right = new XML("condition");
				// Left = A=>B
				left.addChild(curr.getChild(0));
				left.addChild(curr.getChild(1));
				// Right = B=>A
				right.addChild(curr.getChild(1));
				right.addChild(curr.getChild(0));
				// Change original bicondition to (left&&right)
				curr.removeChild(curr.getChild(1));
				curr.removeChild(curr.getChild(0));
				curr.addChild(left);
				curr.addChild(right);
				// Change node from 'bicondition' to 'and'
				curr.setName("and");
			}
		}
	}
	
	
	// Replace all conditions with truth preserving disjunctions.
	public void eliminateConditions()
	{
		eliminateConditionsRecursive(tree);
		dirtyTree = true; // View changes in Processing window
	}
	
	
	// Recursively replace conditions with truth preserving disjunctions
	public void eliminateConditionsRecursive(XML node)
	{
		XML[] children = node.getChildren();
		// Search for conditions
		// Base Case: leaf node (no children)
		for(int i = 0; i < children.length; i++) {
			XML curr = children[i];
			// Recurse
			eliminateConditionsRecursive(curr);
			// Replace condition with truth preserving disjunction
			if(curr.getName().equals("condition")) {
				// curr = A=>B will become (!A||B) //
				// Replace A with (!A)
				XML not = new XML("not");
				not.addChild(curr.getChild(0));
				curr.addChild(not);
				curr.removeChild(curr.getChild(0));
				// Change the node from 'condition' to 'or'
				curr.setName("or");
			}
		}
	}
	
	
	// Move negations in a truth preserving way to apply only to literals.
	public void moveNegationInwards()
	{
		moveNegationInwardsRecursive(tree);
		dirtyTree = true; // View changes in Processing window
	}
	
	
	// Recursively move negations in a truth preserving way to apply only to literals
	public void moveNegationInwardsRecursive(XML node)
	{
		XML[] children = node.getChildren();
		boolean child_change = false;
		boolean any_child_changed = false;
		// Travel down tree searching for not's
		for(int i = 0; i < children.length; i++) {
			XML curr = children[i];
			any_child_changed = false;
			// Locate a not node
			if(curr.getName().equals("not")) {
				child_change = false;
				// Keep any easy access variable to not's child
				XML not_child = curr.getChild(0); 
				// Double negative found:
				if(not_child.getName().equals("not")) {
					// curr = !!A will become A
					node.addChild(not_child.getChild(0));
					// Return formatting for looping
					if(i == 0) {
						node.addChild(node.getChild(1));
						node.removeChild(node.getChild(1));
					}
					node.removeChild(node.getChild(i));
					child_change = true;
				}
				// DeMorgan's Law:
				else if(not_child.getName().equals("and")||not_child.getName().equals("or")){
					// curr = !(A||B) or !(A&&B) will become (!A&&!B) or (!A||!B)
					XML not1 = new XML("not");
					XML not2 = new XML("not");
					// Convert A and B to !A and !B
					not1.addChild(not_child.getChild(0));
					not2.addChild(not_child.getChild(1));
					not_child.removeChild(not_child.getChild(0));
					not_child.removeChild(not_child.getChild(0));
					not_child.addChild(not1);
					not_child.addChild(not2);
					// DeMorgans law change
					if(not_child.getName().equals("and")) {
						not_child.setName("or");
					}
					else if(not_child.getName().equals("or")) {
						not_child.setName("and");
					}
					// Remove the not from the tree
					node.addChild(not_child);
					// Return formatting for looping
					if(i == 0) {
						node.addChild(node.getChild(1));
						node.removeChild(node.getChild(1));
					}
					node.removeChild(node.getChild(i));
					child_change = true;
				}
				// Recurse if child changed
				if(child_change) {
					any_child_changed = true;
					moveNegationInwardsRecursive(node);
				}
			}
			// Recurse if nothing changed (avoids recursing on a node twice)
			if(!any_child_changed){
				moveNegationInwardsRecursive(curr);
			}
		}
	}
	
	
	// Move negations in a truth preserving way to apply only to literals.
	public void distributeOrsOverAnds()
	{
		distributeOrsoverAndsRecursive(tree);
		dirtyTree = true; // View changes in Processing window
	}
	
	
	// Recursively move negations in a truth preserving way to apply only to literals
	public void distributeOrsoverAndsRecursive(XML tree)
	{
		XML[] children = tree.getChildren();
		boolean change_happened = false;
		boolean any_change_happened = false;
		// Travel down tree searching for or's
		for(int i = 0; i < children.length; i++) {
			XML curr = children[i];
			if(curr.getName().equals("or")) {
				change_happened = false;
				XML left = curr.getChild(0);
				XML right = curr.getChild(1);
				if(left.getName().equals("and")) {
					// curr = (X&&Y)||Z will become (X||Z)&&(Y||Z)
					XML or1 = new XML("or");
					XML or2 = new XML("or");
					or1.addChild(left.getChild(0));
					or1.addChild(right);
					or2.addChild(left.getChild(1));
					or2.addChild(right);
					curr.removeChild(left);
					curr.removeChild(right);
					curr.addChild(or1);
					curr.addChild(or2);
					curr.setName("and");
					change_happened = true;
				}
				// Update left and right in case or has two and children
				left = curr.getChild(0);
				right = curr.getChild(1);
				if(right.getName().equals("and")) {
					// curr = X||(Y&&Z) will become (X||Y)&&(X||Z)
					XML or1 = new XML("or");
					XML or2 = new XML("or");
					or1.addChild(left);
					or1.addChild(right.getChild(0));
					or2.addChild(left);
					or2.addChild(right.getChild(1));
					curr.removeChild(left);
					curr.removeChild(right);
					curr.addChild(or1);
					curr.addChild(or2);
					curr.setName("and");
					change_happened = true;
				}
				// Recurse on same node if any change happened
				if(change_happened) {
					any_change_happened = true;
					distributeOrsoverAndsRecursive(tree);
				}
			}
			// Recurse on child if nothing changed
			if(!any_change_happened) {
				distributeOrsoverAndsRecursive(curr);
			}
		}
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
	public void unnestAndsOrs(XML node) {
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
	public void guaranteeFormat() {
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
	public void removeRedundancy(XML set) {
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
	public void removeRedundantLiterals(XML clause) {
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
	public void removeTautologies(XML set) {
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
	public XML resolve(XML clause1, XML clause2)
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
	public boolean isLiteralNegated(XML literal) 
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
	public String getAtomFromLiteral(XML literal)
	{
		if(literal.getName().equals("not")) {
			return literal.getChild(0).getName();
		}
		return literal.getName();
	}	
	

	// Returns true when the provided clause contains a literal
	// with the atomic name and negation (isNegated).  Otherwise, returns false.
	public boolean clauseContainsLiteral(XML clause, String atom, boolean isNegated)
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
	public boolean setContainsClause(XML set, XML clause)
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
	public boolean clauseIsTautology(XML clause)
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
