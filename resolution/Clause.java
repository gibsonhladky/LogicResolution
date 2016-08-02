package resolution;

import java.util.ArrayList;
import java.util.List;

import processing.data.XML;

public class Clause {

	private XML root;
	private List<Literal> literals;
	
	public Clause(XML clauseRoot) {
		root = clauseRoot;
		literals = new ArrayList<Literal>(root.getChildCount());
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
	
	public XML resolveWith(XML other) {
		Clause otherClause = new Clause(other);
		XML resolvent = new XML("or");
		for(Literal literal : literals) {
			if(!otherClause.containsInverseOfLiteral(literal)) {
				resolvent.addChild(literal.toXML());
			}
		}
		for(Literal literal : otherClause.literals) {
			if(!containsInverseOfLiteral(literal)) {
				resolvent.addChild(literal.toXML());
			}
		}
		new Clause(resolvent).removeRedundantLiterals();
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
	
	// TODO: Break dependency on root here
	public void removeRedundantLiterals() {
		for (int i = 0; i < root.getChildCount(); i++) {
			removeMultiplesOfLiteral(root.getChild(i));
		}
	}
	
	private void removeMultiplesOfLiteral(XML literal) {
		for(XML child : root.getChildren()) {
			if(new Literal(child).equals(new Literal(literal)) && !child.equals(literal)) {
				root.removeChild(child);
				literals.remove(new Literal(child));
			}
		}
	}
}
