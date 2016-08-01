package resolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import processing.data.XML;

public class Set {

	private XML root;
	
	public Set(XML setRoot) {
		root = setRoot;
	}
	
	public List<XML> getClauses() {
		return Arrays.asList(root.getChildren());
	}
	
	public void removeRedundantClauses() {
		ArrayList<XML> nonRedundantClauses = new ArrayList<XML>();
		for (XML tempClause : root.getChildren()) {
			root.removeChild(tempClause);
			if (!containsClause(tempClause)) {
				nonRedundantClauses.add(tempClause);
			}
		}
		// Add back all non-redundant clauses
		for (int i = 0; i < nonRedundantClauses.size(); i++) {
			root.addChild(nonRedundantClauses.get(i));
		}
	}

	private boolean containsClause(XML clauseToMatch) {
		for (XML actualClause : root.getChildren()) {
			if(new Clause(clauseToMatch).equals(new Clause(actualClause))) {
				return true;
			}
		}
		return false;
	}
	
	public void removeRedundancy() {
		for (XML clause : root.getChildren()) {
			new Clause(clause).removeRedundantLiteralsIn();
		}
		removeRedundantClauses();
	}

	public void removeTautologies() {
		for (XML clause : root.getChildren()) {
			if (clauseIsTautology(clause)) {
				root.removeChild(clause);
			}
		}
	}
	
	// Tautology: a clause that is true regardless of the variables,
	// for example: A || !A
	private boolean clauseIsTautology(XML clause) {
		for (XML literal : clause.getChildren()) {
			if (new Clause(clause).containsInverseOfLiteral(literal)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean canBeResolvedFurther() {
		for (XML clause1 : root.getChildren()) {
			for (XML clause2 : root.getChildren()) {
				if (new Clause(clause1).canBeResolvedWith(new Clause(clause2))){
					XML resolvent = resolventOf(clause1, clause2);
					// Add new valid non-duplicate resolvents
					if (!containsClause(resolvent)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean createResolvents() {
		boolean wasUpdated = false;
		for (XML clause1 : root.getChildren()) {
			for (XML clause2 : root.getChildren()) {
				if (new Clause(clause1).canBeResolvedWith(new Clause(clause2))){
					XML resolvent = resolventOf(clause1, clause2);
					// Add new valid non-duplicate resolvents
					if (!containsClause(resolvent)) {
						root.addChild(resolvent);
						wasUpdated = true;
					}
				}
			}
		}
		return wasUpdated;
	}
	
	// Returns the resolvent of two overlapping clauses.
	private XML resolventOf(XML clause1, XML clause2) {
		XML resolvent = new XML("or");
		for(XML literal : clause1.getChildren()) {
			if(!new Clause(clause2).containsInverseOfLiteral(literal)) {
				resolvent.addChild(literal);
			}
		}
		for(XML literal : clause2.getChildren()) {
			if(!new Clause(clause1).containsInverseOfLiteral(literal)) {
				resolvent.addChild(literal);
			}
		}
		new Clause(resolvent).removeRedundantLiteralsIn();
		return resolvent;
	}

	public boolean setContainsConflictingClauses() {
		for (XML clause1 : root.getChildren()) {
			for (XML clause2 : root.getChildren()) {
				if (new Clause(clause1).conflictsWith(new Clause(clause2))) {
					return true;
				}
			}
		}
		return false;
	}
}
