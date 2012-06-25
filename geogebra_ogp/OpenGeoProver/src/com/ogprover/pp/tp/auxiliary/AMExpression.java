/* 
 * DISCLAIMER PLACEHOLDER 
 */
package com.ogprover.pp.tp.auxiliary;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import com.ogprover.pp.tp.geoconstruction.Point;

/**
 * <dl>
 * <dt><b>Class description:</b></dt>
 * <dd>Class for representing expressions used in the area method :
 * 		those expressions can be integers, ratios of two collinear segments,
 * 		area of a triangle, pythagoras difference between three points, or 
 * 		any rational expression formed by those primitives.</dd>
 * </dl>
 * 
 * @version 1.00
 * @author Damien Desfontaines
 */
public abstract class AMExpression {
	/*
	 * ======================================================================
	 * ========================== VARIABLES =================================
	 * ======================================================================
	 */
	/**
	 * <i><b>
	 * Version number of class in form xx.yy where
	 * xx is major version/release number and yy is minor
	 * release number.
	 * </b></i>
	 */
	public static final String VERSION_NUM = "1.00"; // this should match the version number from class comment
	
	
	/*
	 * ======================================================================
	 * ========================= ABSTRACT METHODS ===========================
	 * ======================================================================
	 */
	/**
	 * @return a String corresponding to the expression.
	 */
	public abstract String print();

	/**
	 * @return the set of points used in this expression.
	 */
	public abstract HashSet<Point> getPoints();
	
	/**
	 * @return true iff this expression contains only free points
	 */
	public abstract boolean containsOnlyFreePoints();
	
	/**
	 * @return the expression uniformized
	 */
	public abstract AMExpression uniformize();
	
	/**
	 * @return the expression in which one point has been eliminated
	 */
	public abstract AMExpression eliminate(Point pt);
	
	/**
	 * @return the expression in the form AMFraction(a,b), where a and b do not contain any AMFraction.
	 */
	public abstract AMExpression reduceToSingleFraction();
	
	/**
	 * An expression containing additions and products is said to be in a 
	 * right associative form if it is of the form 
	 * 		expr = (c1*x1*...*xn_1 + c2*y1*...*yn_2 + ... + cm*z1*...*zn_m)
	 * where ci are constants, and xi, yi and zi are of type AMAreaOfTriangle, 
	 * or AMPythagorasDifference, and where the big sum and all the products 
	 * are in a right associative form.
	 * @return the expression transformed into a right associative form.
	 * /!\ This method is supposed to be called on an object without any fraction left.
	 */
	public abstract AMExpression reductToRightAssociativeForm();
	
	/*
	 * ======================================================================
	 * ======================= COMMON OBJECT METHODS ========================
	 * ======================================================================
	 */
	public abstract boolean equals(AMExpression expr);
	
	
	/*
	 * ======================================================================
	 * ========================== SPECIFIC METHODS ==========================
	 * ======================================================================
	 */
	/**
	 * @return true iff this expression is equal to (new AMNumber(0))
	 */
	public boolean isZero() {
		if (this instanceof AMNumber) {
			if (((AMNumber)this).value() == 0) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * See http://hal.inria.fr/hal-00426563/PDF/areaMethodRecapV2.pdf for the list of all possible simplifications
	 * @return the expression one-step-simplified
	 */
	public AMExpression simplifyInOneStep() {
		if (this instanceof AMRatio) {
			AMRatio ratio = (AMRatio)this;
			if (ratio.getA() == ratio.getB())
				return new AMNumber(0); // AA/CD -> 0
		}
		if (this instanceof AMAreaOfTriangle) {
			AMAreaOfTriangle area = (AMAreaOfTriangle)this;
			if (area.getA() == area.getB() || area.getA() == area.getC() || area.getB() == area.getC())
				return new AMNumber(0); // S_ABA -> 0, S_AAB -> 0, S_BAA -> 0
		}
		if (this instanceof AMPythagorasDifference) {
			AMPythagorasDifference difference = (AMPythagorasDifference)this;
			if (difference.getA() == difference.getB() || difference.getB() == difference.getC())
				return new AMNumber(0); // P_AAB -> 0, P_BAA -> 0
		}
		if (this instanceof AMProduct) {
			AMProduct product = (AMProduct)this;
			AMExpression factor1 = product.getFactor1().simplifyInOneStep();
			AMExpression factor2 = product.getFactor2().simplifyInOneStep();
			if (factor1 instanceof AMNumber) {
				if (factor2 instanceof AMNumber)
					return new AMNumber(((AMNumber)factor1).value() * ((AMNumber)factor2).value()); // n.n' -> n*n'
				int value = ((AMNumber)factor1).value();
				if (value == 0)
					return new AMNumber(0); // 0.a -> 0
				if (value == 1)
					return factor2; // 1.a -> a
				if (value < 0)
					return new AMAdditiveInverse(new AMProduct(new AMNumber(-value), factor2)); // (-n).a -> -(n.a)
			}
			if (factor2 instanceof AMNumber) {
				int value = ((AMNumber)factor2).value();
				if (value == 0)
					return new AMNumber(0); // a.0 -> 0
				if (value == 1)
					return factor1; // a.1 -> a
				if (value < 0)
					return new AMAdditiveInverse(new AMProduct(new AMNumber(-value), factor1)); // a.(-n) -> -(n.a)
			}
			if (factor1 instanceof AMAdditiveInverse) {
				if (factor2 instanceof AMAdditiveInverse)
					return new AMProduct(((AMAdditiveInverse)factor1).getExpr(), 
										 ((AMAdditiveInverse)factor2).getExpr()); // (-a).(-b) -> a.b
				return new AMAdditiveInverse(new AMProduct(((AMAdditiveInverse)factor1).getExpr(), factor2)); // (-a).b -> -(a.b)
			}
			if (factor2 instanceof AMAdditiveInverse)
				return new AMAdditiveInverse(new AMProduct(((AMAdditiveInverse)factor2).getExpr(), factor1)); // a.(-b) -> -(a.b)))
			if (factor1 instanceof AMFraction) {
				AMExpression numerator = ((AMFraction)factor1).getNumerator();
				AMExpression denominator = ((AMFraction)factor1).getDenominator();
				if (numerator.equals(new AMNumber(1)) && denominator.equals(factor2))
					return new AMNumber(1); // a.(1/a) -> 1
			}
			if (factor2 instanceof AMFraction) {
				AMExpression numerator = ((AMFraction)factor2).getNumerator();
				AMExpression denominator = ((AMFraction)factor2).getDenominator();
				if (numerator.equals(new AMNumber(1)) && denominator.equals(factor1))
					return new AMNumber(1); // (1/a).a -> 1
			}
			return new AMProduct(factor1, factor2);
		}
		if (this instanceof AMSum) {
			AMSum sum = (AMSum)this;
			AMExpression term1 = sum.getTerm1().simplifyInOneStep();
			AMExpression term2 = sum.getTerm2().simplifyInOneStep();
			if (term1.isZero())
				return term2; // 0+a -> a
			if (term2.isZero())
				return term1; // a+0 -> a
			if (term2 instanceof AMAdditiveInverse)
				return new AMDifference(term1, ((AMAdditiveInverse)term2).getExpr()); // a+(-b) -> a-b
			if (term1 instanceof AMAdditiveInverse)
				return new AMDifference(term2, ((AMAdditiveInverse)term1).getExpr()); // (-a)+b -> b-a
			if (term1 instanceof AMNumber && term2 instanceof AMNumber)
					return new AMNumber(((AMNumber)term1).value() + ((AMNumber)term2).value()); // n+n' -> n+n'
			return new AMSum(term1, term2);
		}
		if (this instanceof AMDifference) {
			AMDifference difference = (AMDifference)this;
			AMExpression term1 = difference.getTerm1().simplifyInOneStep();
			AMExpression term2 = difference.getTerm2().simplifyInOneStep();
			if (term2.isZero())
				return term1; // a-0 -> a
			if (term1.isZero())
				return new AMAdditiveInverse(term2); // 0-a -> -a
			if (term1.equals(term2))
				return new AMNumber(0); // a-a -> 0
			if (term1 instanceof AMNumber && term2 instanceof AMNumber)
					return new AMNumber(((AMNumber)term1).value() - ((AMNumber)term2).value()); // n-n' -> n-n'
			return new AMDifference(term1, term2);
		}
		if (this instanceof AMAdditiveInverse) {
			AMExpression expr = (((AMAdditiveInverse)this).getExpr()).simplifyInOneStep();
			if (expr instanceof AMAdditiveInverse)
				return ((AMAdditiveInverse)expr).getExpr(); // --a -> a
			if (expr.isZero())
				return new AMNumber(0); // -0 -> 0
			return new AMAdditiveInverse(expr);
		}
		if (this instanceof AMFraction) {
			AMFraction fraction = (AMFraction)this;
			AMExpression numerator = fraction.getNumerator().simplifyInOneStep();
			AMExpression denominator = fraction.getDenominator().simplifyInOneStep();
			if (numerator.isZero())
				return new AMNumber(0); // 0/a -> 0
			if (denominator.isZero()) {
				System.out.println("Division by zero in expression : " + this.print());
				return null;
			}
			if (numerator.equals(denominator))
				return new AMNumber(1); // a/a -> 0
			if (denominator.equals(new AMNumber(1)))
				return numerator; // a/1 -> a
			if (numerator instanceof AMAdditiveInverse) {
				if (denominator instanceof AMAdditiveInverse)
					return new AMFraction(((AMAdditiveInverse)numerator).getExpr(), ((AMAdditiveInverse)denominator).getExpr()); // (-a)/(-b) -> a/b
				return new AMAdditiveInverse(new AMFraction(((AMAdditiveInverse)numerator).getExpr(), denominator)); // (-a)/b -> -(a/b)
			}
			if (denominator instanceof AMAdditiveInverse)
				return new AMAdditiveInverse(new AMFraction(numerator, ((AMAdditiveInverse)denominator).getExpr())); // a/(-b) -> -(a/b)
			if (numerator instanceof AMProduct) {
				AMProduct product = (AMProduct)numerator;
				AMExpression factor1 = product.getFactor1().simplifyInOneStep();
				AMExpression factor2 = product.getFactor2().simplifyInOneStep();
				if (factor1.equals(denominator))
					return factor2; // (a*b)/a -> b
				if (factor2.equals(denominator))
					return factor1; // (b*a)/a -> b
			}
			return new AMFraction(numerator, denominator);
		}
		return this;
	}
	
	/**
	 * @return the expression fully simplified
	 */
	public AMExpression simplify() {
		AMExpression last = this; 
		AMExpression current = last.simplifyInOneStep();
		while (!last.equals(current)) {
			last = current;
			current = current.simplifyInOneStep();
		}
		return last;
	}
	
	/**
	 * @return the list of the factors of this product
	 * /!\ The expression has to be a product, in right associative form, with (or without) a single constant on the left.
	 */
	public List<AMExpression> productToList() {
		if (this instanceof AMPythagorasDifference || this instanceof AMAreaOfTriangle) {
			List<AMExpression> list = new Vector<AMExpression>(); 
			list.add(this);
			return list;
		}
		
		if (this instanceof AMNumber)
			return new Vector<AMExpression>();
		
		if (this instanceof AMProduct) {
			AMExpression leftFactor = ((AMProduct)this).getFactor1();
			AMExpression rightFactor = ((AMProduct)this).getFactor2();
			List<AMExpression> list = rightFactor.productToList();
			if (leftFactor instanceof AMPythagorasDifference || leftFactor instanceof AMAreaOfTriangle)
				list.add(leftFactor);
			if (leftFactor instanceof AMNumber)
				return list;
		}
		
		System.out.println("The expression is not in the good form : " + this.print());
		return null;
	}
	
	/**
	 * @return true iff the given product is the same as this. 
	 * /!\ The expressions has to be products, in right associative form, with (or without) a single constant on the left
	 */
	public boolean isSameProduct(AMExpression expr) {
		List<AMExpression> factorsOfThis = this.productToList();
		List<AMExpression> factorsOfExpr = expr.productToList();
		AMExpressionComparator comparator = new AMExpressionComparator();
		Collections.sort(factorsOfExpr, comparator);
		Collections.sort(factorsOfThis, comparator);
		
		if (factorsOfThis.size() != factorsOfExpr.size())
			return false;
		for (int i=0 ; i<factorsOfThis.size() ; i++)
			if (!(factorsOfThis.get(i).equals(factorsOfExpr.get(i))))
				return false;
		return true;
		//return factorsOfThis.equals(factorsOfExpr);
	}
	
	/**
	 * If this is a sum of products in right associative form, where all the products contain one single 
	 * constant on the left, add the given expression to the sum, without repetition of products.
	 * For example, if this = 2*x*y + 4*z and expr = -7*x*y, this.addProductToSum(AMExpression expr) will be
	 * equal to -3*x*y + 4*z. This works even if the factors of the products are not in the same order (e.g.
	 * if expr = -7*y*z.
	 */
	public AMExpression addProductToSum(AMExpression expr) {
		System.out.println("  " + this.print() + ".addProductToSum(" + expr.print() + ")");
		if(this instanceof AMSum) {
			System.out.println("    case 1 : this[" + this.print() + "] instanceof AMSum");
			AMExpression leftTerm = ((AMSum)this).getTerm1();
			AMExpression restOfSum = ((AMSum)this).getTerm2();
			if (leftTerm.isSameProduct(expr)) {
				int constantOfLeftTerm = ((AMNumber)((AMProduct)leftTerm).getFactor1()).value();
				int constantOfExpr =  ((AMNumber)((AMProduct)expr).getFactor1()).value();
				int sum = constantOfExpr + constantOfLeftTerm;
				AMExpression restOfProduct = ((AMProduct)leftTerm).getFactor2();
				return new AMSum(new AMProduct(new AMNumber(sum), restOfProduct), restOfSum);
			}
			return new AMSum(leftTerm, restOfSum.addProductToSum(expr));
		}
		if (this.isSameProduct(expr)) {
			int constantOfThis = ((AMNumber)((AMProduct)this).getFactor1()).value();
			int constantOfExpr =  ((AMNumber)((AMProduct)expr).getFactor1()).value();
			int sum = constantOfExpr + constantOfThis;
			AMExpression restOfProduct = ((AMProduct)this).getFactor2();
			return new AMProduct(new AMNumber(sum), restOfProduct);
		}
		return new AMSum(this, expr);
	}
	
	/**
	 * If this is a sum of products, groups the terms which are equal up to a constant multiplicative factor.
	 */
	public AMExpression groupSumOfProducts() {
		if (this instanceof AMSum) {
			AMExpression leftTerm = ((AMSum)this).getTerm1();
			AMExpression groupedRest = ((AMSum)this).getTerm2().groupSumOfProducts();
			return groupedRest.addProductToSum(leftTerm);
		}
		return this;
	}
}