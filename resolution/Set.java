package resolution;

import java.util.ArrayList;
import java.util.List;

import processing.data.XML;
import tests.XMLMatcher;

public class Set {

	private XML root;
	private List<Clause> clauses;
	
	public Set(XML setRoot) {
		root = setRoot;
		clauses = new ArrayList<Clause>(setRoot.getChildCount());
		removeRedundancy();
	}
	
	public XML toXML() {
		XML root = new XML("and");
		for(Clause clause : clauses) {
			root.addChild(clause.toXML());
		}
		if(!XMLMatcher.equivalentTo(root).matches(this.root)) {
			System.out.println("expected: " + this.root + "\n but was: " + root + "\n");
		}
		return root;
	}
	
	private void removeRedundantClauses() {
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
	
	private void removeRedundancy() {
		for (XML clause : root.getChildren()) {
			Clause newClause = new Clause(clause);
			newClause.removeRedundantLiterals();
			replaceChildWith(clause, newClause.toXML());
		}
		removeRedundantClauses();
		removeTautologies();
	}

	private void replaceChildWith(XML child, XML replacement) {
		child.getParent().addChild(replacement);
		child.getParent().removeChild(child);
	}

	private void removeTautologies() {
		for (XML clause : root.getChildren()) {
			if (new Clause(clause).isTautology()) {
				root.removeChild(clause);
				clauses.remove(new Clause(clause));
			}
			else {
				clauses.add(new Clause(clause));
			}
		}
	}
	
	public void resolve() {
		while (canBeResolvedFurther()) {
			createResolvents();
		}
	}
	
	private boolean canBeResolvedFurther() {
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
	
	private void createResolvents() {
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
	
	public boolean containsConflict() {
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
