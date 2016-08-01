package resolution;

import processing.data.XML;

public class Literal {
	
	private XML root;
	
	public Literal(XML rootNode) {
		root = rootNode;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Literal)) {
			return false;
		}
		Literal otherLiteral = (Literal) other;
		return atom().equals(otherLiteral.atom()) && 
				isNegated() == otherLiteral.isNegated();
	}
	
	public boolean isInverseOf(Literal other) {
		return atom().equals(other.atom()) &&
				isNegated() != other.isNegated();
	}
	
	private boolean isNegated() {
		if (root.getName().equals("not")) {
			return true;
		}
		return false;
	}
	
	private String atom() {
		if (root.getName().equals("not")) {
			return root.getChild(0).getName();
		}
		return root.getName();
	}
	
}
