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

	@Test
	public void simpleBiconditionalEliminated() {
		givenInput("A <=> B");
		resolution.eliminateBiconditions();
		assertLogicMatches("(A => B) && (B => A)");
	}
	
	@Test
	public void simpleConditionalEliminated() {
		givenInput("A => B");
		resolution.eliminateConditions();
		assertLogicMatches("B || (! A)");
	}
	
	@Test
	public void moveNegationInwardsRemovesDoubleNot() {
		givenInput("!! A");
		resolution.moveNegationInwards();
		assertLogicMatches("A");
	}
	
	@Test
	public void deMorgansOverAnd() {
		givenInput("!(A && B)");
		resolution.moveNegationInwards();
		assertLogicMatches("(!A) || (!B)");
	}
	
	@Test
	public void deMorgansOverOr() {
		givenInput("!(A || B)");
		resolution.moveNegationInwards();
		assertLogicMatches("(!A) && (!B)");
	}
	
	@Test
	public void simpleDistributeOrsOverAnds() {
		givenInput("( A && B) || C");
		resolution.distributeOrsOverAnds();
		assertLogicMatches("(A || C) && (B || C)");
	}
	
	private void givenInput(String input) {
		actual = LogicParser.toXML(input);
		resolution = new Resolution(applet, actual);
	}
	
	/*
	 * XML's equals() method is not implemented, but
	 * it's toString() is, so matching strings is a simple
	 * work around to checking equals.
	 */
	private void assertLogicMatches(String expected) {
		assertEquals(LogicParser.toXML(expected).toString(), actual.toString());
	}

}
