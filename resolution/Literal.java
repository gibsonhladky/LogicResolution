package resolution;

import processing.data.XML;

public class Literal {
	
	private String atom;
	private boolean isNegated;
	
	private Literal(String atom, boolean isNegated) {
		this.atom = atom;
		this.isNegated = isNegated;
	}
	
	public static Literal fromXML(XML literalRoot) {
		String atom = literalRoot.getName().equals("not") ? literalRoot.getChild(0).getName() : literalRoot.getName();
		boolean isNegated = literalRoot.getName().equals("not");
		return new Literal(atom, isNegated);
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
	
}
