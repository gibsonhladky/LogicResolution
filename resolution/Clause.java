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
			if (!otherClause.containsLiteral(expectedLiteral)) {
				return false;
			}
		}
		return true;
	}

	private boolean containsLiteral(XML literal) {
		for (XML child : root.getChildren()) {
			if (literalsMatch(child, literal)) {
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
			if (isLiteralNegated(child) != isLiteralNegated(literal) && atomOf(child).equals(atomOf(literal))) {
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
	
	private boolean isLiteralNegated(XML literal) {
		if (literal.getName().equals("not")) {
			return true;
		}
		return false;
	}
	
	private String atomOf(XML literal) {
		if (literal.getName().equals("not")) {
			return literal.getChild(0).getName();
		}
		return literal.getName();
	}
	
	public void removeRedundantLiteralsIn() {
		for (int i = 0; i < root.getChildCount(); i++) {
			removeMultiplesOfLiteral(root.getChild(i));
		}
	}
	
	private void removeMultiplesOfLiteral(XML literal) {
		for(XML child : root.getChildren()) {
			if(literalsMatch(child, literal) && !child.equals(literal)) {
				root.removeChild(child);
			}
		}
	}
	
	private boolean literalsMatch(XML actual, XML expected) {
		return atomOf(actual).equals(atomOf(expected)) && 
				isLiteralNegated(actual) == isLiteralNegated(expected);
	}
}
