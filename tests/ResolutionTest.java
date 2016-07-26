package tests;

import static org.junit.Assert.*;

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

	public void complexDistributeOrsOverAnds() {
		givenInput("((A && B) || C) || (C && D)");
		// ((A && B) || C) || (C && D)
		// (((A && B) || C) || C) && (((A && B) || C) || D)
		// (((A || C) && (B || C)) || C) && (((A || C) && (B || C)) || D)
		// (((A || C) || C)  && ((B || C) || C)) && (((A || C) || D) && ((B || C) || D))
		resolution.distributeOrsOverAnds();
		assertLogicMatches("(((A || C) || C) && ((B || C) || C)) && (((A || C) || D) && ((B || C) || D))");
	}
	
	@Test public void simpleCollapse() {
		givenInput("(A || B) && ((B || C) && (A || C))");
		resolution.collapse();
		assertCollapsedLogicMatches("<logic><and><or><A/><B/></or><or><B/><C/></or><or><A/><C/></or></and></logic>");
	}
	
	@Test public void applyResolutionDetectsSimpleConflict() {
		givenInput("(A) && (!A)");
		resolution.collapse();
		assertTrue(resolution.applyResolution());
	}
	
	@Test public void applyResolutionDetectsComplexConflict() {
		givenInput("(A || B || !C) && (A || !B) && (!A || C)");
		resolution.collapse();
		assertTrue(resolution.applyResolution());
	}
	
	@Test public void applyResolutionFindsNoConflicts() {
		givenInput("(A || B || !C) && (A || !B) && (A || C)");
		resolution.collapse();
		assertFalse(resolution.applyResolution());
	}
	
	@Test public void applyResolutionFindsNoResolvents() {
		givenInput("(A || B) && (A || C)");
		resolution.collapse();
		assertCollapsedLogicMatches("<logic><and><or><A/><B/></or><or><A/><C/></or></and></logic>");
	}

	// TODO: Test for applyResolution adding resolvents to the tree
	// TODO: Figure out what resolvents are...
	
	private void givenInput(String input) {
		actual = LogicParser.toXML(input);
		resolution = new Resolution(applet, actual);
	}

	// XML's equals() method is not implemented, but it's toString() is, so
	// matching strings is a simple work around to checking equals.
	private void assertLogicMatches(String expected) {
		assertEquals(LogicParser.toXML(expected).toString(), actual.toString());
	}
	
	private void assertCollapsedLogicMatches(String expected) {
		assertEquals(expected, actual.toString());
	}

}
