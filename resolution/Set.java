package resolution;

import java.util.ArrayList;
import java.util.List;

import processing.data.XML;

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
			addNewResolventsFrom(createNewResolvents());
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
	
	private List<Clause> createNewResolvents() {
		List<Clause> newResolvents = new ArrayList<Clause>();
		for (Clause clause1 : clauses) {
			for (Clause clause2 : clauses) {
				if (clause1.canBeResolvedWith(clause2)){
					Clause resolvent = clause1.resolveWith(clause2);
					if (!newResolvents.contains(resolvent)) {
						newResolvents.add(resolvent);
					}
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
