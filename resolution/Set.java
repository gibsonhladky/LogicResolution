package resolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import processing.data.XML;

public class Set {

	private XML set;
	
	public Set(XML setRoot) {
		set = setRoot;
	}
	
	public List<XML> getClauses() {
		return Arrays.asList(set.getChildren());
	}
	
	public void removeRedundantClauses() {
		ArrayList<XML> nonRedundantClauses = new ArrayList<XML>();
		for (XML tempClause : set.getChildren()) {
			set.removeChild(tempClause);
			if (!containsClause(tempClause)) {
				nonRedundantClauses.add(tempClause);
			}
		}
		// Add back all non-redundant clauses
		for (int i = 0; i < nonRedundantClauses.size(); i++) {
			set.addChild(nonRedundantClauses.get(i));
		}
	}

	private boolean containsClause(XML clauseToMatch) {
		for (XML actualClause : set.getChildren()) {
			if(clausesMatch(actualClause, clauseToMatch)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean clausesMatch(XML actualClause, XML expectedClause) {
		if(actualClause.getChildCount() != expectedClause.getChildCount()) {
			return false;
		}
		for(XML expectedLiteral : expectedClause.getChildren()) {
			if (!clauseContainsLiteral(actualClause, expectedLiteral)) {
				return false;
			}
		}
		return true;
	}

	private boolean clauseContainsLiteral(XML clause, XML literal) {
		for (XML child : clause.getChildren()) {
			if (literalsMatch(child, literal)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean literalsMatch(XML actual, XML expected) {
		return atomOf(actual).equals(atomOf(expected)) && 
				isLiteralNegated(actual) == isLiteralNegated(expected);
	}
	
	private String atomOf(XML literal) {
		if (literal.getName().equals("not")) {
			return literal.getChild(0).getName();
		}
		return literal.getName();
	}
	
	private boolean isLiteralNegated(XML literal) {
		if (literal.getName().equals("not")) {
			return true;
		}
		return false;
	}
	
	public void removeRedundancy() {
		for (XML clause : set.getChildren()) {
			removeRedundantLiteralsIn(clause);
		}
		removeRedundantClauses();
	}

	public void removeTautologies() {
		for (XML clause : set.getChildren()) {
			if (clauseIsTautology(clause)) {
				set.removeChild(clause);
			}
		}
	}
	
	// Tautology: a clause that is true regardless of the variables,
	// for example: A || !A
	private boolean clauseIsTautology(XML clause) {
		for (XML literal : clause.getChildren()) {
			if (clauseContainsInverseOfLiteral(clause, literal)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean canBeResolvedFurther() {
		for (XML clause1 : set.getChildren()) {
			for (XML clause2 : set.getChildren()) {
				if (clausesCanBeResolved(clause1, clause2)){
					XML resolvent = resolventOf(clause1, clause2);
					// Add new valid non-duplicate resolvents
					if (!setContainsClause(set, resolvent)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean createResolvents() {
		boolean wasUpdated = false;
		for (XML clause1 : set.getChildren()) {
			for (XML clause2 : set.getChildren()) {
				if (clausesCanBeResolved(clause1, clause2)){
					XML resolvent = resolventOf(clause1, clause2);
					// Add new valid non-duplicate resolvents
					if (!setContainsClause(set, resolvent)) {
						set.addChild(resolvent);
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
			if(!clauseContainsInverseOfLiteral(clause2, literal)) {
				resolvent.addChild(literal);
			}
		}
		for(XML literal : clause2.getChildren()) {
			if(!clauseContainsInverseOfLiteral(clause1, literal)) {
				resolvent.addChild(literal);
			}
		}
		removeRedundantLiteralsIn(resolvent);
		return resolvent;
	}

	private boolean setContainsClause(XML set, XML clauseToMatch) {
		for (XML actualClause : set.getChildren()) {
			if(clausesMatch(actualClause, clauseToMatch)) {
				return true;
			}
		}
		return false;
	}
	
	// Removes all redundant literals from a clause
	private void removeRedundantLiteralsIn(XML clause) {
		for (int i = 0; i < clause.getChildCount(); i++) {
			removeMultiplesOfLiteralInClause(clause.getChild(i), clause);
		}
	}
	
	private void removeMultiplesOfLiteralInClause(XML literal, XML clause) {
		for(XML child : clause.getChildren()) {
			if(literalsMatch(child, literal) && !child.equals(literal)) {
				clause.removeChild(child);
			}
		}
	}

	private boolean clausesCanBeResolved(XML clause1, XML clause2) {
		for(XML literal : clause1.getChildren()) {
			if(clauseContainsInverseOfLiteral(clause2, literal)) {
				return !clausesConflict(clause1, clause2);
			}
		}
		return false;
	}
	
	public boolean setContainsConflictingClauses() {
		for (XML clause1 : set.getChildren()) {
			for (XML clause2 : set.getChildren()) {
				if (clausesConflict(clause1, clause2)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean clausesConflict(XML clause1, XML clause2) {
		for(XML literal : clause1.getChildren()) {
			if(!clauseContainsInverseOfLiteral(clause2, literal)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean clauseContainsInverseOfLiteral(XML clause, XML literal) {
		for (XML child : clause.getChildren()) {
			if (isLiteralNegated(child) != isLiteralNegated(literal) && atomOf(child).equals(atomOf(literal))) {
				return true;
			}
		}
		return false;
	}
}
