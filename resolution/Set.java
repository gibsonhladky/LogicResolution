package resolution;

import java.util.ArrayList;
import java.util.List;

import processing.data.XML;

public class Set {

	private List<Clause> clauses;
	
	public Set(XML setRoot) {
		clauses = new ArrayList<Clause>();
		addAllClausesFrom(setRoot);
		removeRedundancy();
	}
	
	private void addAllClausesFrom(XML root) {
		for(XML clauseNode : root.getChildren()) {
			clauses.add(new Clause(clauseNode));
		}
	}
	
	public XML toXML() {
		XML root = new XML("and");
		for(Clause clause : clauses) {
			root.addChild(clause.toXML());
		}
		return root;
	}
	
	private void removeRedundancy() {
		for (Clause clause : clauses) {
			clause.removeRedundantLiterals();
		}
		removeRedundantClauses();
		removeTautologies();
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

	private void removeTautologies() {
		List<Clause> tautologies = new ArrayList<Clause>();
		for (Clause clause : clauses) {
			if (clause.isTautology()) {
				tautologies.add(clause);
			}
		}
		clauses.removeAll(tautologies);
	}
	
	public void resolve() {
		while (canBeResolvedFurther()) {
			createResolvents();
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
	
	private void createResolvents() {
		List<Clause> newResolvents = new ArrayList<Clause>();
		for (Clause clause1 : clauses) {
			for (Clause clause2 : clauses) {
				if (clause1.canBeResolvedWith(clause2)){
					Clause resolvent = clause1.resolveWith(clause2);
					// Add new valid non-duplicate resolvents
					if (!clauses.contains(newResolvents)) {
						newResolvents.add(resolvent);
					}
				}
			}
		}
		for(Clause clause : newResolvents) {
			if(!clauses.contains(clause)) {
				clauses.add(clause);
			}
		}
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
}
