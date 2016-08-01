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
			new Clause(clause).removeRedundantLiterals();
		}
		removeRedundantClauses();
	}

	public void removeTautologies() {
		for (XML clause : root.getChildren()) {
			if (new Clause(clause).isTautology()) {
				root.removeChild(clause);
			}
		}
	}
	
	public boolean canBeResolvedFurther() {
		for (XML clause1 : root.getChildren()) {
			for (XML clause2 : root.getChildren()) {
				if (new Clause(clause1).canBeResolvedWith(new Clause(clause2))){
					XML resolvent = new Clause(clause1).resolveWith(clause2);
					// Add new valid non-duplicate resolvents
					if (!containsClause(resolvent)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void createResolvents() {
		for (XML clause1 : root.getChildren()) {
			for (XML clause2 : root.getChildren()) {
				if (new Clause(clause1).canBeResolvedWith(new Clause(clause2))){
					XML resolvent = new Clause(clause1).resolveWith(clause2);
					// Add new valid non-duplicate resolvents
					if (!containsClause(resolvent)) {
						root.addChild(resolvent);
					}
				}
			}
		}
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
