package resolution;

import processing.data.XML;

/*
 * A Literal has an atom (name) to reference it.
 * A Literal may or may not be negated.
 */
public class Literal {
	
	private String atom;
	private boolean isNegated;
	
	private Literal(String atom, boolean isNegated) {
		this.atom = atom;
		this.isNegated = isNegated;
	}
	
	/*
	 * Note: The XML node must be either a single node with the Literal's name,
	 * or a single "not" node with a single child with the Literal's name.
	 */
	public static Literal fromXML(XML literalRoot) {
		String atom = literalRoot.getName().equals("not") ? literalRoot.getChild(0).getName() : literalRoot.getName();
		boolean isNegated = literalRoot.getName().equals("not");
		return new Literal(atom, isNegated);
	}
	
	/*
	 * Literals are equal if their atom's and negations are equal.
	 */
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
