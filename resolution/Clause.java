package resolution;

import java.util.ArrayList;
import java.util.List;

import processing.data.XML;

public class Clause {

	private List<Literal> literals;
	
	public Clause(XML clauseRoot) {
		literals = new ArrayList<Literal>(clauseRoot.getChildCount());
		addLiteralsFrom(clauseRoot);
	}
	
	private void addLiteralsFrom(XML clauseRoot) {
		for(XML literalNode : clauseRoot.getChildren()) {
			literals.add(new Literal(literalNode));
		}
	}
	
	public XML toXML() {
		XML root = new XML("or");
		for(Literal literal : literals) {
			root.addChild(literal.toXML());
		}
		return root;
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
	
	public boolean containsInverseOfLiteral(Literal expectedLiteral) {
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
		XML resolventNode = new XML("or");
		for(Literal literal : literals) {
			if(!other.containsInverseOfLiteral(literal)) {
				resolventNode.addChild(literal.toXML());
			}
		}
		for(Literal literal : other.literals) {
			if(!containsInverseOfLiteral(literal)) {
				resolventNode.addChild(literal.toXML());
			}
		}
		Clause resolvent = new Clause(resolventNode);
		resolvent.removeRedundantLiterals();
		return resolvent;
	}
	
	// Tautology: a clause that is true regardless of the variables,
	// for example: A || !A
	public boolean isTautology() {
		for (Literal literal : literals) {
			if (containsInverseOfLiteral(literal)) {
				return true;
			}
		}
		return false;
	}
	
	public void removeRedundantLiterals() {
		List<Literal> nonRedundantLiterals = new ArrayList<Literal>();
		for (Literal literal : literals) {
			if(!nonRedundantLiterals.contains(literal)) {
				nonRedundantLiterals.add(literal);
			}
		}
		literals = nonRedundantLiterals;
	}
}
