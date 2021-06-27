package com.newjob.parser.algo;

import java.util.LinkedList;

public class FindCorrespondingClosingBracket {

    public static int find(String input, int startIdx, char openingBracket) {
        char closingBracket = Character.MIN_VALUE;
        if (openingBracket == '(')
            closingBracket = ')';
        else if (openingBracket == '[')
            closingBracket = ']';
        else if (openingBracket == '{') {
            closingBracket = '}';
        } else {
            throw new IllegalArgumentException("Unknown bracket type");
        }

        if (input.charAt(startIdx) != openingBracket)
            throw new IllegalArgumentException("StartIdx has no opening bracket");

        LinkedList<Character> stack = new LinkedList<>();
        stack.push(openingBracket);

        for (int i = startIdx + 1; i < input.length(); i++) {
            if (input.charAt(i) == openingBracket) {
                stack.push(openingBracket);
            } else if (input.charAt(i) == closingBracket) {
                stack.pop();
                if (stack.isEmpty()) {
                    return i; // Return the index of closing bracket
                }
            }
        }

        // Corresponding closing bracket has not been found
        return -1;
    }

}
