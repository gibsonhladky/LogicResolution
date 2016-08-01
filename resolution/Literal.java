package resolution;

import processing.data.XML;

public class Literal {
	
	private String atom;
	private boolean isNegated;
	
	public Literal(XML root) {
		atom = root.getName().equals("not") ? root.getChild(0).getName() : root.getName();
		isNegated = root.getName().equals("not");
	}
	
	public XML toXML() {
		XML literal;
		if(isNegated) {
			literal = new XML("not");
			literal.addChild(atom);
		}
		else {
			literal = new XML(atom);
		}
		return literal;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Literal)) {
			return false;
		}
		Literal otherLiteral = (Literal) other;
		return atom.equals(otherLiteral.atom) && 
				isNegated == otherLiteral.isNegated;
	}
	
	public boolean isInverseOf(Literal other) {
		return atom.equals(other.atom) &&
				isNegated != other.isNegated;
	}
	
}
