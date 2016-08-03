package resolution;

import java.util.ArrayList;
import java.util.List;

import processing.data.XML;

public class Clause {

	private List<Literal> literals;
	
	private Clause(List<Literal> literals) {
		this.literals = literals;
		removeRedundantLiterals();
	}
	
	private void removeRedundantLiterals() {
		List<Literal> nonRedundantLiterals = new ArrayList<Literal>();
		for (Literal literal : literals) {
			if(!nonRedundantLiterals.contains(literal)) {
				nonRedundantLiterals.add(literal);
			}
		}
		literals = nonRedundantLiterals;
	}
	
	public static Clause fromXML(XML clauseRoot) {
		return new Clause(literalsFromXML(clauseRoot.getChildren()));
	}
	
	private static List<Literal> literalsFromXML(XML[] literalRoots) {
		List<Literal> literals = new ArrayList<Literal>(literalRoots.length);
		for(XML literalRoot : literalRoots) {
			literals.add(Literal.fromXML(literalRoot));
		}
		return literals;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Clause)) {
			return false;
		}
		Clause otherClause = (Clause) other;
		if(otherClause.literals.size() != literals.size()) {
			return false;
		}
		for(Literal literal : literals) {
			if (!otherClause.clauseContainsLiteral(literal)) {
				return false;
			}
		}
		return true;
	}

	private boolean clauseContainsLiteral(Literal expectedLiteral) {
		for (Literal literal : literals) {
			if (literal.equals(expectedLiteral)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean canBeResolvedWith(Clause other) {
		for(Literal literal : literals) {
			if(other.containsInverseOfLiteral(literal)) {
				return !conflictsWith(other);
			}
		}
		return false;
	}
	
	private boolean containsInverseOfLiteral(Literal expectedLiteral) {
		for (Literal literal : literals) {
			if (literal.isInverseOf(expectedLiteral)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean conflictsWith(Clause other) {
		for(Literal literal : literals) {
			if(!other.containsInverseOfLiteral(literal)) {
				return false;
			}
		}
		return true;
	}
	
	public Clause resolveWith(Clause other) {
		List<Literal> resolvedLiterals = new ArrayList<Literal>();
		resolvedLiterals.addAll(literalsWhenResolvedWith(other));
		resolvedLiterals.addAll(other.literalsWhenResolvedWith(this));
		
		Clause resolvent = new Clause(resolvedLiterals);
		resolvent.removeRedundantLiterals();
		return resolvent;
	}
	
	private List<Literal> literalsWhenResolvedWith(Clause other) {
		List<Literal> resolvedLiterals = new ArrayList<Literal>();
		for(Literal literal : literals) {
			if(!other.containsInverseOfLiteral(literal)) {
				resolvedLiterals.add(literal);
			}
		}
		return resolvedLiterals;
	}
	
	// Tautology is a clause that is true regardless of the variables,
	// for example: A || !A
	public boolean isTautology() {
		for (Literal literal : literals) {
			if (containsInverseOfLiteral(literal)) {
				return true;
			}
		}
		return false;
	}
	
	public XML toXML() {
		XML root = new XML("or");
		for(Literal literal : literals) {
			root.addChild(literal.toXML());
		}
		return root;
	}
}
