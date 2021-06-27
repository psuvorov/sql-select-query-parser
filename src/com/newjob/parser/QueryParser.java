package com.newjob.parser;

import com.newjob.parser.algo.BalancedBracketsChecker;
import com.newjob.parser.algo.FindCorrespondingClosingBracket;
import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.terms.Column;
import com.newjob.parser.exceptions.InvalidQueryFormatException;

public class QueryParser {

    private String rawQuery;

    private final Query resultQuery = new Query();

    private static final class Keywords {
        private static final String select = "select";
        private static final String from = "from";
        private static final String join = "join";
        private static final String innerJoin = "inner join";
        private static final String leftJoin = "left join";
        private static final String rightJoin = "right join";
        private static final String fullJoin = "full join";
        private static final String where = "where";
        private static final String orderBy = "order by";
        private static final String groupBy = "group by";
        private static final String having = "having";
        private static final String limit = "limit";
        private static final String offset = "offset";
    }

    public static Query parseQuery(String query) throws InvalidQueryFormatException {
        return new QueryParser().parseQuery(query, true);
    }

    private Query parseQuery(String query, boolean normalize) throws InvalidQueryFormatException {
        if (query == null || query.isEmpty())
            throw new InvalidQueryFormatException();

        rawQuery = query;

        if (normalize)
            normalize();
        validate();
        performParsing();

        return resultQuery;
    }

    private void normalize() {
        rawQuery = rawQuery
                .trim()
                .replaceAll("\\s+", " ")
                .replace("( ", "(")
                .replace(" )", ")")
                .replaceAll(".from", ", from")
                .toLowerCase();
    }

    private void validate() throws InvalidQueryFormatException {
        int selectIdx = rawQuery.indexOf("select");
        if (selectIdx == -1)
            throw new InvalidQueryFormatException("Query doesn't contain 'select' statement");

        int fromIdx = rawQuery.indexOf("from");
        if (fromIdx == -1)
            throw new InvalidQueryFormatException("Query doesn't contain 'from' statement");

        if (!BalancedBracketsChecker.isBalanced(rawQuery))
            throw new InvalidQueryFormatException();
    }

    private void performParsing() throws InvalidQueryFormatException {
        System.out.println(rawQuery);

        int sourceStartIdx = extractColumns();

        System.out.println("");
    }

    // Find all select members respective to the current level select statement
    // If we find inner select,
    private int extractColumns() throws InvalidQueryFormatException {
        int startIdx = Keywords.select.length() + 1;

        for (int i = startIdx; i < rawQuery.length(); i++) {
            char c = rawQuery.charAt(i);
            if (c == ' ')
                continue;

            if (c != '(') {
                if (rawQuery.substring(i, i + Keywords.from.length()).equals(Keywords.from)) {
                    // From keyword reached
                    return  i + Keywords.from.length() + 1;
                } else {
                    int j = i + 1;
                    while (rawQuery.charAt(j) != ',') {
                        j++;
                    }
                    String simpleColumnTerm = rawQuery.substring(i, j);
                    resultQuery.addColumn(new Column(simpleColumnTerm));
                    i = j + 1;
                }
            } else if ((i + 1) + Keywords.select.length() < rawQuery.length() && rawQuery.substring(i + 1, (i + 1) + Keywords.select.length()).equals(Keywords.select)) {
                // Check whether it's a subquery

                int closingBracketIdx = FindCorrespondingClosingBracket.find(rawQuery, i, '(');
                String subQueryInSelectStatement = rawQuery.substring(i, closingBracketIdx + 1);

                try {
                    // Then set the current column to be subquery
                    resultQuery.addColumn(new Column((new QueryParser()).parseQuery(subQueryInSelectStatement.substring(1, subQueryInSelectStatement.length() - 1), false)));
                } catch(InvalidQueryFormatException ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println(i);
            }
        }

        throw new InvalidQueryFormatException("Malformed query, select keyword is missing");
    }

}
