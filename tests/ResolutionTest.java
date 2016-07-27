package tests;

import static org.junit.Assert.*;
import static tests.XMLMatcher.equivalentTo;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

import processing.core.PApplet;
import processing.data.XML;
import resolution.LogicParser;
import resolution.Resolution;

public class ResolutionTest {

	PApplet applet = new PApplet();

	Resolution resolution;
	
	XML actual;

	@Test public void simpleBiconditionalEliminated() {
		givenInput("A <=> B");
		resolution.eliminateBiconditions();
		assertLogicMatches("(A => B) && (B => A)");
	}

	@Test public void complexBiconditionalEliminated() {
		givenInput("(A || B) <=> !(A && C)");
		resolution.eliminateBiconditions();
		assertLogicMatches("((A || B) => !(A && C)) && (!(A && C) => (A || B))");
	}

	@Test public void nestedBiconditions() {
		givenInput("A <=> (B <=> C)");
		resolution.eliminateBiconditions();
		assertLogicMatches("(A => ((B => C) && (C => B))) && (((B => C) && (C => B)) => A)");
	}

	@Test public void simpleConditionalEliminated() {
		givenInput("A => B");
		resolution.eliminateConditions();
		assertLogicMatches("B || (! A)");
	}

	@Test public void complexConditionalEliminated() {
		givenInput("(A || B) => !(A && C)");
		resolution.eliminateConditions();
		assertLogicMatches("!(A && C) || !(A || B)");
	}

	@Test public void nestedConditionalEliminated() {
		givenInput("A => (B => C)");
		resolution.eliminateConditions();
		assertLogicMatches("(C || !B) || !A");
	}

	@Test public void removeDoubleNot() {
		givenInput("!! A");
		resolution.moveAllNegationsInwards();
		assertLogicMatches("A");
	}

	@Test public void deMorgansOverAnd() {
		givenInput("!(A && B)");
		resolution.moveAllNegationsInwards();
		assertLogicMatches("(!A) || (!B)");
	}

	@Test public void deMorgansOverOr() {
		givenInput("!(A || B)");
		resolution.moveAllNegationsInwards();
		assertLogicMatches("(!A) && (!B)");
	}

	@Test public void complexMoveNegationInwards() {
		givenInput("!(A && !(B || !C))");
		resolution.moveAllNegationsInwards();
		assertLogicMatches("!A || (B || !C)");
	}

	@Test public void simpleDistributeOrsOverAnds() {
		givenInput("(!A && B) || (B && C)");
		resolution.distributeOrsOverAnds();
		assertLogicMatches("((!A || B) && (!A || C)) && ((B || B) && (B || C))");
	}

	@Test public void complexDistributeOrsOverAnds() {
		givenInput("((A && B) || C) || (C && D)");
		// ((A && B) || C) || (C && D)
		// (((A && B) || C) || C) && (((A && B) || C) || D)
		// (((A || C) && (B || C)) || C) && (((A || C) && (B || C)) || D)
		// (((A || C) || C)  && ((B || C) || C)) && (((A || C) || D) && ((B || C) || D))
		resolution.distributeOrsOverAnds();
		assertLogicMatches("(((A || C) || C) && ((B || C) || C)) && (((A || C) || D) && ((B || C) || D))");
	}
	
	@Test public void simpleCollapse() {
		givenInput("(A || B) && ((B || C) && A)");
		resolution.collapse();
		assertCollapsedLogicMatches("<logic><and><or><A/><B/></or><or><B/><C/></or><or><A/></or></and></logic>");
	}
	
	@Test public void complexCollapse() {
		givenInput("(A || B || C) && (D || E) && F");
		resolution.collapse();
		assertCollapsedLogicMatches("<logic><and><or><A/><B/><C/></or><or><D/><E/></or><or><F/></or></and></logic>");
	}
	
	@Test public void applyResolutionDetectsSimpleConflict() {
		givenInput("(A) && (!A)");
		resolution.collapse();
		assertTrue(resolution.applyResolution());
	}
	
	@Test public void applyResolutionDetectsComplexConflict() {
		givenInput("(!A || C) && (!B || C) && (A || B) && !C");
		resolution.collapse();
		assertTrue(resolution.applyResolution());
		assertCollapsedLogicMatches(""
				+ "<logic>"
				+ 	"<and>"
				+ 		"<or>"
				+ 			"<not><A/></not>"
				+ 			"<C/>"
				+ 		"</or>"
				+ 		"<or>"
				+ 			"<not><B/></not>"
				+ 			"<C/>"
				+ 		"</or>"
				+ 		"<or>"
				+ 			"<A/>"
				+ 			"<B/>"
				+ 		"</or>"
				+ 		"<or>"
				+ 			"<not><C/></not>"
				+ 		"</or>"
				+ 		"<or>"
				+ 			"<B/>"
				+ 			"<C/>"
				+ 		"</or>"
				+ 		"<or>"
				+ 			"<not><A/></not>"
				+ 		"</or>"
				+ 		"<or>"
				+ 			"<A/>"
				+ 			"<C/>"
				+ 		"</or>"
				+ 		"<or>"
				+ 			"<not><B/></not>"
				+ 		"</or>"
				+ 		"<or>"
				+ 			"<C/>"
				+ 		"</or>"
				+ 	"</and>"
				+ "</logic>");
	}
	
	@Test public void applyResolutionFindsNoConflicts() {
		givenInput("(A || B || !C) && (A || !B) && (A || C)");
		resolution.collapse();
		assertFalse(resolution.applyResolution());
		assertCollapsedLogicMatches("<logic><and><or><A/><B/><not><C/></not></or><or><A/><not><B/></not></or><or><A/><C/></or>"
				+ "<or><A/><not><C/></not></or><or><A/><B/></or>"
				+ "<or><A/></or></and></logic>");
	}
	
	@Test public void applyResolutionFindsNoResolvents() {
		givenInput("(A || B) && (A || C)");
		resolution.collapse();
		assertFalse(resolution.applyResolution());
		assertCollapsedLogicMatches("<logic><and><or><A/><B/></or><or><A/><C/></or></and></logic>");
	}
	
	@Test public void applyResolutionFindsSimpleResolvent() {
		givenInput("(A || B) && (!A || C)");
		resolution.collapse();
		assertFalse(resolution.applyResolution());
		assertCollapsedLogicMatches("<logic><and><or><A/><B/></or><or><not><A/></not><C/></or><or><B/><C/></or></and></logic>");
	}
	
	private void givenInput(String input) {
		actual = LogicParser.toXML(input);
		resolution = new Resolution(applet, actual);
	}

	// XML's equals() method is not implemented, but it's toString() is, so
	// matching strings is a simple work around to checking equals.
	private void assertLogicMatches(String expectedString) {
		XML expected = LogicParser.toXML(expectedString);
		assertThat(actual, is(equivalentTo(expected)));
	}
	
	private void assertCollapsedLogicMatches(String expectedString) {
		XML expected = new PApplet().parseXML(expectedString);
		assertThat(actual, is(equivalentTo(expected)));
	}

}
