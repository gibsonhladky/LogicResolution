package resolution;

import java.util.ArrayList;
import java.util.List;

import processing.data.XML;

/*
 * A Clause is a disjunction of Literals.
 * A Clause does not contain any redundant Literals.
 */
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
	
	private static Clause withLiterals(List<Literal> literals) {
		return new Clause(literals);
	}
	
	/*
	 * Note: The XML node must have only the Literals of the Clause as children.
	 * Any name or attributes are ignored for the Clause.
	 */
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
	
	/*
	 * A Clause can only be equal to another Clause.
	 * Clauses are equal if they contain all of each others Literals.
	 */
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Clause)) {
			return false;
		}
		return equals((Clause) other);
	}
	
	private boolean equals(Clause other) {
		if(other.literals.size() != literals.size()) {
			return false;
		}
		for(Literal literal : literals) {
			if (!other.clauseContainsLiteral(literal)) {
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
	
	/*
	 * Two Clauses can be resolved if they contain inverse Literals
	 * and do not conflict.
	 */
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
	
	/*
	 * Clauses conflict if each contains the inverse of the other's Literals.
	 */
	public boolean conflictsWith(Clause other) {
		for(Literal literal : literals) {
			if(!other.containsInverseOfLiteral(literal)) {
				return false;
			}
		}
		return true;
	}
	
	/*
	 * Produces a new Clause containing the Literals from each Clause
	 * that the other did not have an inverse of.
	 */
	public Clause resolveWith(Clause other) {
		Clause resolvent = Clause.withLiterals(literalsWhenResolvedWith(other));
		resolvent.removeRedundantLiterals();
		return resolvent;
	}
	
	private List<Literal> literalsWhenResolvedWith(Clause other) {
		List<Literal> resolvedLiterals = new ArrayList<Literal>();
		// TODO: extract method to eliminate duplication.
		for(Literal literal : literals) {
			if(!other.containsInverseOfLiteral(literal)) {
				resolvedLiterals.add(literal);
			}
		}
		for(Literal literal : other.literals) {
			if(!containsInverseOfLiteral(literal)) {
				resolvedLiterals.add(literal);
			}
		}
		return resolvedLiterals;
	}
	
	/*
	 * Returns true if this Clause contains any Literal whose inverse
	 * is also in the Clause.
	 */
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
