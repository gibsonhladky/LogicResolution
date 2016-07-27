package tests;

import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;

import processing.data.XML;

public class XMLMatcher extends org.hamcrest.TypeSafeMatcher<XML> {

	private final XML expectedRoot;
	
	public XMLMatcher(XML expectedTree) {
		this.expectedRoot = expectedTree;
	}

	@Override
	protected boolean matchesSafely(XML actualRoot) {
		return matchesNodeName(actualRoot.getName()) && containsSameChildrenAs(actualRoot);
	}
	
	private boolean matchesNodeName(String actualName) {
		return expectedRoot.getName().equals(actualName);
	}
	
	private boolean containsSameChildrenAs(XML actualRoot) {
		return hasSameNumberOfChildrenAs(actualRoot) && allChildrenMatch(actualRoot);
	}
	
	private boolean hasSameNumberOfChildrenAs(XML actualRoot) {
		return expectedRoot.getChildCount() == actualRoot.getChildCount();
	}
	
	private boolean allChildrenMatch(XML actualRoot) {
		for(XML actualChild : actualRoot.getChildren()) {
			if(!anyChildMatches(actualChild)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean anyChildMatches(XML actualChild) {
		for(XML expectedChild : expectedRoot.getChildren()) {
			if(new XMLMatcher(expectedChild).matchesSafely(actualChild)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("<" + expectedRoot.toString() + ">");
		
	}
	
	static public XMLMatcher equivalentTo(XML expectedTree) {
		return new XMLMatcher(expectedTree);
	}

}
