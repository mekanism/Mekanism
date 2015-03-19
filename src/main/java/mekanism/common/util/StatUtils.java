package mekanism.common.util;

import java.util.Random;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.Math.PI;
import static java.lang.Math.E;

public class StatUtils
{
	public static Random rand = new Random();

	public static int inversePoisson(double mean)
	{
		double r = rand.nextDouble()*exp(mean);
		int m = 0;
		double p = 1;
		double stirlingValue = mean*E;
		double stirlingCoeff = 1/sqrt(2*PI);
		
		while((p < r) && (m < 3*ceil(mean)))
		{
			m++;
			p += stirlingCoeff/sqrt(m)*pow((stirlingValue/m), m);
		}
		
		return m;
	}
}
