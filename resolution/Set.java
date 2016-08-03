package resolution;

import java.util.ArrayList;
import java.util.List;

import processing.data.XML;

/*
 * A Set is a conjunction of one or more unique Clauses (disjunctions).
 * A Set does not contain any redundant Clauses, nor any tautologies.
 */
public class Set {

	private List<Clause> clauses;
	
	private Set(List<Clause> clauses) {
		this.clauses = clauses;
		removeRedundancy();
	}
	
	private void removeRedundancy() {
		removeTautologies();
		removeRedundantClauses();
	}

	private void removeTautologies() {
		List<Clause> tautologies = new ArrayList<Clause>();
		for (Clause clause : clauses) {
			if (clause.isTautology()) {
				tautologies.add(clause);
			}
		}
		clauses.removeAll(tautologies);
	}
	
	private void removeRedundantClauses() {
		List<Clause> nonRedundantClauses = new ArrayList<Clause>();
		for (Clause clause : clauses) {
			if (!nonRedundantClauses.contains(clause)) {
				nonRedundantClauses.add(clause);
			}
		}
		clauses = nonRedundantClauses;
	}
	
	/*
	 * Note: The XML node must have only the Clauses of the Set as children.
	 * Any name or attributes are ignored for the Set.
	 */
	public static Set fromXML(XML setRoot) {
		return new Set(clausesFromXML(setRoot.getChildren()));
	}
	
	private static List<Clause> clausesFromXML(XML[] clauseRoots) {
		List<Clause> clauses = new ArrayList<Clause>(clauseRoots.length);
		for(XML clauseRoot : clauseRoots) {
			clauses.add(Clause.fromXML(clauseRoot));
		}
		return clauses;
	}
	
	/*
	 * Returns true if any clause in this set conflicts with any other
	 * clause in this set.
	 * Note: The result of this function is only guaranteed to be accurate
	 * if the Set has already been resolved.
	 */
	public boolean containsConflict() {
		for (Clause clause1 : clauses) {
			for (Clause clause2 : clauses) {
				if (clause1.conflictsWith(clause2)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void resolve() {
		while (canBeResolvedFurther()) {
			addNewResolventsFrom(generateResolvents());
		}
	}
	
	private boolean canBeResolvedFurther() {
		for (Clause clause1 : clauses) {
			for (Clause clause2 : clauses) {
				if (clause1.canBeResolvedWith(clause2)){
					Clause resolvent = clause1.resolveWith(clause2);
					// Add new valid non-duplicate resolvents
					if (!clauses.contains(resolvent)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private void addNewResolventsFrom(List<Clause> newResolvents) {
		for(Clause clause : newResolvents) {
			if(!clauses.contains(clause)) {
				clauses.add(clause);
			}
		}
	}
	
	private List<Clause> generateResolvents() {
		List<Clause> newResolvents = new ArrayList<Clause>();
		for (Clause clause1 : clauses) {
			for (Clause clause2 : clauses) {
				if (clause1.canBeResolvedWith(clause2)){
					Clause resolvent = clause1.resolveWith(clause2);
					newResolvents.add(resolvent);
				}
			}
		}
		return newResolvents;
	}
	
	public XML toXML() {
		XML root = new XML("and");
		for(Clause clause : clauses) {
			root.addChild(clause.toXML());
		}
		return root;
	}
}
