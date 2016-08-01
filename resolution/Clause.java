package resolution;

import processing.data.XML;

public class Clause {

	private XML root;
	
	public Clause(XML clauseRoot) {
		root = clauseRoot;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Clause)) {
			return false;
		}
		Clause otherClause = (Clause) other;
		if(otherClause.root.getChildCount() != root.getChildCount()) {
			return false;
		}
		for(XML expectedLiteral : root.getChildren()) {
			if (!otherClause.clauseContainsLiteral(expectedLiteral)) {
				return false;
			}
		}
		return true;
	}

	private boolean clauseContainsLiteral(XML literal) {
		for (XML child : root.getChildren()) {
			if (new Literal(child).equals(new Literal(literal))) {
				return true;
			}
		}
		return false;
	}
	
	public boolean canBeResolvedWith(Clause other) {
		for(XML literal : root.getChildren()) {
			if(other.containsInverseOfLiteral(literal)) {
				return !conflictsWith(other);
			}
		}
		return false;
	}
	
	public boolean containsInverseOfLiteral(XML literal) {
		for (XML child : root.getChildren()) {
			if (new Literal(child).isInverseOf(new Literal(literal))) {
				return true;
			}
		}
		return false;
	}
	
	public boolean conflictsWith(Clause other) {
		for(XML literal : root.getChildren()) {
			if(!other.containsInverseOfLiteral(literal)) {
				return false;
			}
		}
		return true;
	}
	
	public XML resolveWith(XML other) {
		XML resolvent = new XML("or");
		for(XML literal : root.getChildren()) {
			if(!new Clause(other).containsInverseOfLiteral(literal)) {
				resolvent.addChild(literal);
			}
		}
		for(XML literal : other.getChildren()) {
			if(!containsInverseOfLiteral(literal)) {
				resolvent.addChild(literal);
			}
		}
		new Clause(resolvent).removeRedundantLiterals();
		return resolvent;
	}
	
	// Tautology: a clause that is true regardless of the variables,
	// for example: A || !A
	public boolean isTautology() {
		for (XML literal : root.getChildren()) {
			if (containsInverseOfLiteral(literal)) {
				return true;
			}
		}
		return false;
	}
	
	public void removeRedundantLiterals() {
		for (int i = 0; i < root.getChildCount(); i++) {
			removeMultiplesOfLiteral(root.getChild(i));
		}
	}
	
	private void removeMultiplesOfLiteral(XML literal) {
		for(XML child : root.getChildren()) {
			if(new Literal(child).equals(new Literal(literal)) && !child.equals(literal)) {
				root.removeChild(child);
			}
		}
	}
}
