package com.newjob.parser.algo;

import java.util.LinkedList;

public class BalancedBracketsChecker {

    public static boolean isBalanced(String s) {
        LinkedList<Character> stack = new LinkedList<>();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '(') {
                stack.push(c);
            } else {
                if (c != ')')
                    continue;

                if (stack.size() == 0)
                    return false;

                char sc = stack.pop();
                if (sc != '(')
                    return false;
            }

        }

        return stack.size() <= 0;
    }

}
