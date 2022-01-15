package calculator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.in;

public class Calculator {
    static boolean isCalculable = true;

    public static void main(String[] args) {
        try (final Scanner kb = new Scanner(in)) {
            Map<String, BigInteger> variables = new LinkedHashMap<>();
            while (true) {
                isCalculable = true;
                String line = kb.nextLine().trim();
                String[] data = line.split("\\s+");
                if (!line.isBlank()) {
                    if (line.contains("=")) {
                        data = line.split("\\s*=\\s*");
                        if (data[0].matches("[A-Za-z]+") && data.length == 2) {
                            try {
                                variables.put(data[0], new BigInteger(data[1]));
                            } catch (NumberFormatException numberFormatException) {
                                if (data[1].matches("[A-Za-z]+")) {
                                    if (variables.get(data[1]) != null) {
                                        variables.put(data[0], variables.get(data[1]));
                                    } else {
                                        System.out.println("Unknown variable");
                                        System.out.println(data[1]);
                                    }
                                } else {
                                    System.out.println("Invalid assignment");
                                }
                            }
                        } else {
                            System.out.println("Invalid assignment");
                        }
                    } else if (line.startsWith("/")) {
                        if (line.equals("/help")) {
                            System.out.println("The program calculates the sum and differences of numbers");
                        } else if (line.equals("/exit")) {
                            System.out.println("Bye!");
                            System.exit(0);
                        } else {
                            System.out.println("Unknown command");
                        }
                    } else if (data.length == 1 && line.matches("[+-]?\\d+")) {
                        if (data[0].matches("[+-]?\\w+")) {
                            try {
                                System.out.println(Integer.parseInt(data[0]));
                            } catch (NumberFormatException numberFormatException) {
                                System.out.println(Objects.requireNonNullElse(variables.get(data[0]), "Unknown Variable: " + data[0]));
                            }
                        } else if (!line.isBlank()) {
                            System.out.println("Invalid expression");
                            System.out.println(28);
                        }
                    } else if (variables.get(line) != null) {
                        System.out.println(variables.get(line));
                    } else {
                        List<String> calcEquation = parseEquation(line, variables);
                        if (isCalculable) {
                            Deque<String> postFixList = convertToPostfix(calcEquation);
                            BigDecimal total = postFixResult(postFixList);
                            System.out.println(total);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static List<String> parseEquation(String line, Map<String, BigInteger> variables) {
        List<String> calcEquation = new ArrayList<>();
        char[] charArray = line.toCharArray();
        StringBuilder digitsToAdd = new StringBuilder();
        StringBuilder operatorsToAdd = new StringBuilder();
        int remainingParentheses = 0;
        for (int i = 0; i < charArray.length; i++) {
            String toCheck = line.substring(i, i + 1);
            if (!toCheck.isBlank()) {
                if (isOperator(toCheck)) {
                    if (digitsToAdd.length() > 0) {
                        calcEquation.add(String.valueOf(digitsToAdd));
                    }
                    digitsToAdd = new StringBuilder();
                    operatorsToAdd.append(toCheck);
                } else if (toCheck.equals("(") || toCheck.equals(")")) {
                    boolean isAdded = false;
                    if (operatorsToAdd.length() > 0) {
                        calcEquation.add(addAllOperators(operatorsToAdd));
                    }
                    operatorsToAdd = new StringBuilder();
                    if (digitsToAdd.length() > 0) {
                        calcEquation.add(String.valueOf(digitsToAdd));
                    }
                    if (toCheck.equals("(")) {
                        if (!calcEquation.get(calcEquation.size() - 1).equals("(") &&
                                !calcEquation.get(calcEquation.size() - 1).equals("(") &&
                                !isOperator(calcEquation.get(calcEquation.size() - 1))) {
                            calcEquation.add("*");
                        }
                        calcEquation.add("(");
                        remainingParentheses++;
                        isAdded = true;
                    }
                    if (!isAdded) {
                        calcEquation.add(")");
                        remainingParentheses--;
                    }
                    digitsToAdd = new StringBuilder();
                } else if (toCheck.matches("\\d")) {
                    digitsToAdd.append(toCheck);
                    if (operatorsToAdd.length() > 0) {
                        calcEquation.add(addAllOperators(operatorsToAdd));
                    }
                    operatorsToAdd = new StringBuilder();
                } else if (variables.get(toCheck) != null) {
                    String toAdd = String.valueOf(variables.get(toCheck));
                    if (toAdd.startsWith("-") || toAdd.startsWith("+")) {
                        operatorsToAdd.append(toAdd.charAt(0));
                        toAdd = toAdd.replaceFirst("[-+]\\s*", "");
                    }
                    digitsToAdd.append(toAdd);
                    if (operatorsToAdd.length() > 0) {
                        calcEquation.add(addAllOperators(operatorsToAdd));
                    }
                    operatorsToAdd = new StringBuilder();
//                    System.out.println(toAdd);
                } else if (variables.get(toCheck) == null) {
                    System.out.println("Unknown Variable: " + toCheck);
                    isCalculable = false;
                }
            }
        }
        if (remainingParentheses != 0) {
            isCalculable = false;
            System.out.println("Invalid Expression: " + remainingParentheses);
        }
        if (digitsToAdd.length() > 0) {
            calcEquation.add(String.valueOf(digitsToAdd));
        }
        for (String check : calcEquation) {
            if (!check.matches("\\d+") && !check.equals("(") && !check.equals(")")) {
                if (!isOperator(check)) {
                    System.out.println("Invalid Expression: " + check);
                    isCalculable = false;
                    break;
                }
            }
        }
        System.out.println(calcEquation);
        return calcEquation;
    }

    static String addAllOperators(StringBuilder operatorsToAdd) {
        if (operatorsToAdd.length() == 1) {
            return String.valueOf(operatorsToAdd);
        } else if (operatorsToAdd.length() > 1) {
            return parseSigns(operatorsToAdd, getCount(operatorsToAdd));
        }
        return null;
    }

    static long getCount(StringBuilder operatorsToAdd) {
        Matcher matcher = Pattern.compile("[*/^]").matcher(operatorsToAdd);
        boolean isFound = matcher.find();
        if (isFound) {
            return matcher.results().count();
        } else {
            return 0;
        }
    }

    static String parseSigns(StringBuilder operatorsToAdd, long count) {
        if (count > 0) {
            if (count == 1) {
                return String.valueOf(operatorsToAdd);
            } else {
                System.out.println("Invalid Expression: " + count + " " + operatorsToAdd);
                isCalculable = false;
                return "";
            }
        } else {
            if (operatorsToAdd.length() == 1) {
                return String.valueOf(operatorsToAdd);
            } else if (!String.valueOf(operatorsToAdd).contains("*") ||
                    !String.valueOf(operatorsToAdd).contains("/") ||
                    !String.valueOf(operatorsToAdd).contains("^")) {
                boolean sign = simplifySign(String.valueOf(operatorsToAdd));
                return sign ? "+" : "-";
            } else {
                System.out.println("Invalid expression: " + operatorsToAdd);
                isCalculable = false;
                return "";
            }
        }
    }

    static BigDecimal postFixResult(Deque<String> toCalculate) {
        BigDecimal total = BigDecimal.ZERO;
        Deque<BigDecimal> calcHelper = new ArrayDeque<>();
        for (String bit : toCalculate) {
            if (bit.matches("\\d+")) {
                calcHelper.push(new BigDecimal(bit));
            } else if (isOperator(bit)) {
                System.out.println(calcHelper);
                BigDecimal num2 = new BigDecimal(String.valueOf(calcHelper.pop()));
                BigDecimal num1 = new BigDecimal(String.valueOf(calcHelper.pop()));
                switch (bit) {
                    case "+" -> total = num1.add(num2);
                    case "-" -> total = num1.subtract(num2);
                    case "*" -> total = num1.multiply(num2);
                    case "/" -> {
                        RoundingMode roundingMode = RoundingMode.HALF_UP;
                        total = num1.divide(num2, 1, roundingMode);
                    }
                    case "^" -> total = num1.pow(num2.intValue());
                    default -> System.out.println("Invalid expression: " + bit);
                }
                calcHelper.push(total);
            }
        }
        return total;
    }

    static boolean simplifySign(String toProcess) {
        boolean sign = true;
        for (char c : toProcess.toCharArray()) {
            if (c == '-') {
                sign = !sign;
            }
        }
        return sign;
    }

    static Deque<String> convertToPostfix(List<String> data) {
        Deque<String> operatorsStack = new ArrayDeque<>();
        Deque<String> postFixStack = new ArrayDeque<>();
//        int parenthesisDiff = 0;
        for (String toProcess : data) {
            if (isOperator(toProcess)) {
                while (!operatorsStack.isEmpty() && !operatorsStack.peek().equals("(")
                        && isLowerPrecedenceOperator(toProcess, operatorsStack.peek())) {
                    postFixStack.offer(operatorsStack.pop());
                }
                operatorsStack.push(toProcess);
            } else if (toProcess.equals("(")) {
                operatorsStack.push("(");
            } else if (toProcess.equals(")")) {
                System.out.println(postFixStack);
                while (!"(".equals(operatorsStack.peek())) {
                    postFixStack.offer(operatorsStack.pop());
                }
                operatorsStack.pop();
            } else if (toProcess.matches("\\d+")) {
                postFixStack.offer(toProcess);
            } else if (!toProcess.matches("\\s+")) {
                System.out.println(toProcess + " is Ignored!");
                System.out.println(postFixStack);
                System.out.println(operatorsStack);
                System.out.println(data);
                System.exit(0);
            }
        }
        while (!operatorsStack.isEmpty()) {
            postFixStack.offer(operatorsStack.pop());
        }
        System.out.println(postFixStack);
        return postFixStack;
    }

    static boolean isLowerPrecedenceOperator(String operator1, String operator2) {
        int operator1Value = getOperatorValue(operator1);
        int operator2Value = getOperatorValue(operator2);
        return operator1Value <= operator2Value;
    }

    static int getOperatorValue(String operator) {
        return switch (operator) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            case "^" -> 3;
            case "(", ")" -> 100;
            default -> -1;
        };
    }

    static boolean isOperator(String toProcess) {
        return toProcess.equals("+") || toProcess.equals("-") ||
                toProcess.equals("*") || toProcess.equals("/") ||
                toProcess.equals("^");
    }
}
