package tests;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

import processing.core.PApplet;
import processing.data.XML;

import tests.XMLMatcher;

public class XMLMatcherTest {
	
	XML expected, actual;

	@Test
	public void matchesSingleNode() {
		givenExpected("<root/>");
		givenActual("<root/>");
		assertMatch();
	}
	
	@Test
	public void doesNotMatchDifferentNode() {
		givenExpected("<root/>");
		givenActual("<notTheRoot/>");
		assertNoMatch();
	}
	
	@Test
	public void doesNotMatchExtraHierarchy() {
		givenExpected("<root/>");
		
		givenActual(""
				+ "<root>"
				+ 	"<child1/>"
				+ 	"<child2/>"
				+ "</root>");
		
		assertNoMatch();
	}
	
	@Test
	public void doesNotMatchMissingHierarchy() {
		givenExpected(""
				+ "<root>"
				+ 	"<child1/>"
				+ 	"<child2/>"
				+ "</root>");
		
		givenActual("<root/>");
		
		assertNoMatch();
	}
	
	@Test
	public void matchesSimpleSameHierarchy() {
		givenExpected(
				  "<root>"
				+ 	"<child1/>"
				+ 	"<child2/>"
				+ "</root>");
		
		givenActual(""
				+ "<root>"
				+ 	"<child1/>"
				+	"<child2/>"
				+ "</root>");
		
		assertMatch();
	}
	
	@Test
	public void matchesSimpleEquivalentHierarchy() {
		givenExpected(
				  "<root>"
				+ 	"<child1/>"
				+ 	"<child2/>"
				+ "</root>");
		
		givenActual(
				  "<root>"
				+ 	"<child2/>"
				+ 	"<child1/>"
				+ "</root>");
		
		assertMatch();
	}
	
	@Test
	public void doesNotMatchSimpleDifferentHierarchy() {
		givenExpected(
				  "<root>"
				+ 	"<child1/>"
				+ 	"<child2/>"
				+ "</root>");
		
		givenActual(
				  "<root>"
				+ 	"<wrong_child/>"
				+ 	"<child2/>"
				+ "</root>");
		
		assertNoMatch();
	}
	
	@Test
	public void matchesComplexEquivalentHierarchy() {
		givenExpected(
				  "<root>"
				+ 	"<child1>"
				+ 		"<grandchild1/>"
				+ 		"<grandchild2/>"
				+ 	"</child1>"
				+ 	"<child2>"
				+ 		"<grandchild3/>"
				+ 		"<grandchild4/>"
				+ 	"</child2>"
				+ "</root>");
		
		givenActual(
				  "<root>"
				+ 	"<child2>"
				+ 		"<grandchild4/>"
				+ 		"<grandchild3/>"
				+ 	"</child2>"
				+ 	"<child1>"
				+ 		"<grandchild2/>"
				+ 		"<grandchild1/>"
				+ 	"</child1>"
				+ "</root>");
		
		assertMatch();
	}
	
	@Test
	public void doesNotMatchComplexDifferentHierarchy() {
		givenExpected(""
				+ "<root>"
				+ 	"<child1>"
				+ 		"<grandchild1/>"
				+ 		"<grandchild2/>"
				+ 	"</child1>"
				+ 	"<child2>"
				+ 		"<grandchild3/>"
				+ 	"</child2>"
				+ "</root>");
		
		givenActual(""
				+ "<root>"
				+ 	"<child1>"
				+ 		"<wrong_node/>"
				+ 		"<grandchild2/>"
				+ 	"</child1>"
				+ 	"<child2>"
				+ 		"<grandchild3/>"
				+ 	"</child2>"
				+ "</root>");
		
		assertNoMatch();
	}
	
	private void givenExpected(String xmlString) {
		expected = parse(xmlString);
	}
	
	private void givenActual(String actualString) {
		actual = parse(actualString);
	}
	
	private XML parse(String xmlString) {
		PApplet applet = new PApplet();
		return applet.parseXML(xmlString);
	}
	
	private void assertMatch() {
		assertThat(actual, is(equivalentTreeTo(expected)));
	}
	
	private void assertNoMatch() {
		assertThat(actual, is(not(equivalentTreeTo(expected))));
	}
	
	private XMLMatcher equivalentTreeTo(XML expected) {
		return new XMLMatcher(expected);
	}

}
