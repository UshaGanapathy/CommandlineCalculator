package commandlinecalculator;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

public class Calculator {

	final static Logger logger = Logger.getLogger(Calculator.class);
	private static final String illegalArgMsg = "Wrong Input Argument. Correct input format: java Calculator \"add(1, 2)\"";
	private Map<String, LinkedList<Integer>> varMap;

	public Calculator() {
		this.varMap = new HashMap<String, LinkedList<Integer>>();
	}

	int tot = 0;
	int total = 1;

	private int exprEval(String expr) {

		try {
			if (expr.matches("[a-zA-z]+")) {
				if (varMap.containsKey(expr)) {

					System.out.println(varMap.get(expr).peek());

					return varMap.get(expr).peek();
				} else {
					throw new IllegalArgumentException("The variable in let is not found");
				}

			}

			if (isNumeric(expr)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Expression is a number: Expr0 = " + Integer.parseInt(expr));
				}
				return Integer.parseInt(expr);

			} else if (expr.startsWith("add")) {

				List<String> s = getListofExpr(expr, "add");
				for (int i = 0; i < s.size(); i++) {
					tot = exprEval(s.get(0)) + exprEval(s.get(1));
					//System.out.println("add total is" + tot);

				}
				return tot;

			} else if (expr.startsWith("sub")) {

				List<String> s = getListofExpr(expr, "sub");
				for (int i = 0; i < s.size(); i++) {
					tot = exprEval(s.get(0)) - exprEval(s.get(1));
					//System.out.println("sub total is" + tot);
				}
				return tot;

			} else if (expr.startsWith("mul")) {

				List<String> s = getListofExpr(expr, "mul");
				//System.out.println("before mul total is" + tot);
				tot = exprEval(s.get(0)) * exprEval(s.get(1));
				return tot;

			}

			else if (expr.startsWith("div")) {
				List<String> s = getListofExpr(expr, "div");
				tot = exprEval(s.get(0)) / exprEval(s.get(1));
				//System.out.println("div total" + tot);
				return tot;

			} else if (expr.startsWith("let")) {
				String[] exprs = get3LetExpr(expr, "let");
				String label = exprs[0];
				String expr1 = exprs[1];
				String expr2 = exprs[2];
				if (logger.isDebugEnabled()) {
					logger.debug("Let Expression: Label = " + label + ", Expr1 = " + expr1 + ", Expr2 = " + expr2);
				}
				int valExpr1 = exprEval(expr1);
				LinkedList<Integer> currStack;
				if (!varMap.containsKey(label)) {
					currStack = new LinkedList<Integer>();
					varMap.put(label, currStack);
				}
				varMap.get(label).push(valExpr1);

				int valExpr2 = exprEval(expr2);

				LinkedList<Integer> prevStack = varMap.get(label);
				prevStack.pop();
				if (prevStack.isEmpty()) {
					varMap.remove(label);
				}

				return valExpr2;

			}
		} catch (Exception e) {
			logger.error(e);
		}

		return 0;
	}

	/*
	 * Lets check whether the expression has balanced paranthesis,else intimate
	 * the user to correct the input argument
	 */
	private String checkBalancedParentesis(String expr) {
		System.out.println("Expression is " + expr);
		Stack<Character> stack = new Stack<Character>();
		try {
			if (expr.isEmpty())
				return "Balanced";

			for (int i = 0; i < expr.length(); i++) {
				char current = expr.charAt(i);
				if (current == '(') {
					stack.push(current);
				}
				if (current == ')') {
					if (stack.isEmpty()) {
						logger.error("Please check the given input arguments,Paranthesis is Missing");
						return "Not Balanced";
					}

					char last = stack.peek();
					if (current == ')' && last == '(')
						stack.pop();
					else {
						logger.error("Please check the given input arguments,Paranthesis is Missing");
						return "Not Balanced";
					}
				}
			}
			if (!stack.isEmpty()) {

				throw new IllegalArgumentException(illegalArgMsg);
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return stack.isEmpty() ? "Balanced" : "Not Balanced";

	}

	private String[] get3LetExpr(String expr, String op) {
		String[] exprArr = new String[3];

		int commaPos = getNextExpr(expr, op.length() + 1, ',');
		String label = expr.substring(op.length() + 1, commaPos);
		exprArr[0] = label;
		if (logger.isDebugEnabled()) {
			logger.debug("let label = " + label);
		}

		int secondCommaPos = getNextExpr(expr, commaPos + 1, ',');
		String expr1 = expr.substring(commaPos + 1, secondCommaPos);
		exprArr[1] = expr1;

		if (logger.isDebugEnabled()) {
			logger.debug("let expr1 = " + expr1);
		}

		int endPos = getNextExpr(expr, secondCommaPos + 1, ')');
		String expr2 = expr.substring(secondCommaPos + 1, endPos);
		exprArr[2] = expr2;
		if (logger.isDebugEnabled()) {
			logger.debug("let expr2 = " + expr2);
		}

		return exprArr;

	}

	/*
	 * Lets check whether the expression starts with
	 * "add,sub,mul,div and let",else thrw the exxception
	 */
	private boolean checkExprStartswith(String expr) {
		System.out.println("Expression is" + expr);
		String oper = null;
		boolean check = false;
		try {
			List<String> opers = Arrays.asList("ADD", "SUB", "MUL", "DIV", "LET");
			oper = expr.substring(0, 3).toUpperCase();

			if (opers.contains(oper)) {
				check = true;
				return check;
			} else {
				logger.error(
						"Please check the given input arguments,It should be either add/sub/mul/div/let,Example input format: java Calculator \"add(1, 2)\"");
				throw new IllegalArgumentException(illegalArgMsg);

			}
		} catch (Exception e) {
			logger.error(e);
		}
		return check;
	}

	private static List<String> getListofExpr(String expr, String op) {

		List<String> listofExpr = new ArrayList<String>();

		int startPos = getNextExpr(expr, op.length() + 1, ',');

		String expr1 = expr.substring(op.length() + 1, startPos);

		listofExpr.add(expr1);

		int endPos = getNextExpr(expr, startPos + 1, ')');
		String expr2 = expr.substring(startPos + 1, endPos);
		listofExpr.add(expr2);

		/*for (int i = 0; i < listofExpr.size(); i++)
			System.out.println(listofExpr.get(i));*/

		return listofExpr;
	}

	private static int getNextExpr(String expr, int prefix, Character delim) {

		if (logger.isDebugEnabled()) {
			logger.debug("String expr = " + expr);
			logger.debug("prefix value = " + prefix);
			logger.debug("delimiter = " + delim);
		}
		int i = prefix;
		try {
			int paranCounter = 0;
			for (; i < expr.length(); i++) {

				if (paranCounter == 0 && expr.charAt(i) == delim)
					return i;

				if (expr.charAt(i) == '(')
					paranCounter++;

				if (expr.charAt(i) == ')') {
					if (paranCounter == 0)
						throw new IllegalArgumentException(illegalArgMsg);
					paranCounter--;
				}

			}

			if (paranCounter > 0)
				throw new IllegalArgumentException(illegalArgMsg);

		} catch (Exception e) {
			logger.error(e);
		}
		return i;
	}

	/*
	 * check if the expr is a numeric or not
	 */
	private static boolean isNumeric(String expr) {
		String eval = expr;
		if (expr.startsWith("-")) {
			eval = expr.substring(1, expr.length());
		}

		for (Character c : eval.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}

		return true;
	}

	private static void setLogLevel() {
		if (Boolean.getBoolean("log4j.debug")) {
			Configurator.setLevel(System.getProperty("log4j.logger"), Level.DEBUG);
		}
		if (Boolean.getBoolean("log4j.error")) {
			Configurator.setLevel(System.getProperty("log4j.logger"), Level.ERROR);
		}
		if (Boolean.getBoolean("log4j.info")) {
			Configurator.setLevel(System.getProperty("log4j.logger"), Level.INFO);
		}
	}
	
	

	public static void main(String[] args) {
		try {
			if (args.length < 1 || args.length > 1) {
				logger.error(
						"Please check the given input arguments,Program accepts single argument only,Example input format: java Calculator \"add(1, 2)\"");
				throw new IllegalArgumentException();
			}
		} catch (Exception e) {
			logger.error(
					"Please check the given input arguments,It should be either add/sub/mul/div/let,Example input format: java Calculator \"add(1, 2)\"");
			return;
		}
		
		setLogLevel();
		Calculator myCal = new Calculator();

		if (!args[0].isEmpty() || !args[0].equalsIgnoreCase(null)) {

			//System.out.println(args[0].replaceAll("\\s+", ""));
		}
		boolean exprfun = myCal.checkExprStartswith(args[0]);

		if (exprfun) {
			
			System.out.println("Result is : " +myCal.exprEval(args[0].replaceAll("\\s+", "")));

		}
		else
		{
			logger.error(
					"Please check the given input arguments,It should be either add/sub/mul/div/let,Example input format: java Calculator \"add(1, 2)\"");
			
		}
	}

}
