package com.newjob.parser;

import com.newjob.parser.algo.BalancedBracketsChecker;
import com.newjob.parser.algo.FindCorrespondingClosingBracket;
import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.terms.Column;
import com.newjob.parser.domain.terms.Source;
import com.newjob.parser.exceptions.InvalidQueryFormatException;

import java.util.Locale;

public class QueryParser {

    private String rawQuery;

    private final Query resultQuery = new Query();

    private String nextKeywordAfterSourceSection;

    private static final class Keywords {
        private static final String select = "select";
        private static final String from = "from";
        private static final String join = "join";
        private static final String innerJoin = "inner join";
        private static final String leftJoin = "left join";
        private static final String rightJoin = "right join";
        private static final String fullJoin = "full join";
        private static final String on = "on";
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
                .replaceAll(".from", ", from");
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

        int startIdx = Keywords.select.length() + 1;

        int sourceStartIdx = extractColumns(startIdx);
        int nextStatementStartIdx = extractSources(sourceStartIdx);




    }

    // Find all select members respective to the current level select statement
    private int extractColumns(int startIdx) throws InvalidQueryFormatException {

        for (int i = startIdx; i < rawQuery.length(); i++) {
            char c = rawQuery.charAt(i);
            if (c == ' ')
                continue;

            if (c != '(') {
                if (rawQuery.substring(i, i + Keywords.from.length()).equalsIgnoreCase(Keywords.from)) {
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
            } else if ((i + 1) + Keywords.select.length() < rawQuery.length() && rawQuery.substring(i + 1, (i + 1) + Keywords.select.length()).equalsIgnoreCase(Keywords.select)) {
                // Check whether it's a subquery

                int closingBracketIdx = FindCorrespondingClosingBracket.find(rawQuery, i, '(');
                String subQueryInSelectStatement = rawQuery.substring(i, closingBracketIdx + 1);

                try {
                    // Then set the current column to be subquery
                    resultQuery.addColumn(new Column((new QueryParser()).parseQuery(subQueryInSelectStatement.substring(1, subQueryInSelectStatement.length() - 1), false)));
                    i = closingBracketIdx + 1;

                } catch(InvalidQueryFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }

        throw new InvalidQueryFormatException("Malformed query, select keyword is missing");
    }

    private int extractSources(int startIdx) throws InvalidQueryFormatException {
        int i;
        for (i = startIdx; i < rawQuery.length(); i++) {
            char c = rawQuery.charAt(i);
            if (c == ' ')
                continue;

            if (c != '(') {
                // If we bump into 'join' | 'inner join' | 'left join' | 'right join' | 'full join' | 'group by' | 'having' | 'where' | 'limit' | 'offset',
                // then it's the end of sources declaration
                if ((i + 1) + Keywords.join.length() < rawQuery.length() && rawQuery.substring(i + 1, (i + 1) + Keywords.join.length()).equalsIgnoreCase(Keywords.join) ||
                    (i + 1) + Keywords.innerJoin.length() < rawQuery.length() && rawQuery.substring(i + 1, (i + 1) + Keywords.innerJoin.length()).equalsIgnoreCase(Keywords.innerJoin) ||
                    (i + 1) + Keywords.leftJoin.length() < rawQuery.length() && rawQuery.substring(i + 1, (i + 1) + Keywords.leftJoin.length()).equalsIgnoreCase(Keywords.leftJoin) ||
                    (i + 1) + Keywords.rightJoin.length() < rawQuery.length() && rawQuery.substring(i + 1, (i + 1) + Keywords.rightJoin.length()).equalsIgnoreCase(Keywords.rightJoin) ||
                    (i + 1) + Keywords.fullJoin.length() < rawQuery.length() && rawQuery.substring(i + 1, (i + 1) + Keywords.fullJoin.length()).equalsIgnoreCase(Keywords.fullJoin) ||
                    (i + 1) + Keywords.groupBy.length() < rawQuery.length() && rawQuery.substring(i + 1, (i + 1) + Keywords.groupBy.length()).equalsIgnoreCase(Keywords.groupBy) ||
                    (i + 1) + Keywords.having.length() < rawQuery.length() && rawQuery.substring(i + 1, (i + 1) + Keywords.having.length()).equalsIgnoreCase(Keywords.having) ||
                    (i + 1) + Keywords.where.length() < rawQuery.length() && rawQuery.substring(i + 1, (i + 1) + Keywords.where.length()).equalsIgnoreCase(Keywords.where) ||
                    (i + 1) + Keywords.limit.length() < rawQuery.length() && rawQuery.substring(i + 1, (i + 1) + Keywords.limit.length()).equalsIgnoreCase(Keywords.limit) ||
                    (i + 1) + Keywords.offset.length() < rawQuery.length() && rawQuery.substring(i + 1, (i + 1) + Keywords.offset.length()).equalsIgnoreCase(Keywords.offset)
                ) {
                    // TODO: detect the next keyword
                    return i + 1;

                } else {
                    int j = i + 1;
                    while (j < rawQuery.length() &&  rawQuery.charAt(j) != ',') {
                        j++;
                    }
                    String fromSource = rawQuery.substring(i, j);
                    resultQuery.addFromSource(new Source(fromSource));

                    i = j + 1;
                }
            } else if ((i + 1) + Keywords.select.length() < rawQuery.length() && rawQuery.substring(i + 1, (i + 1) + Keywords.select.length()).equalsIgnoreCase(Keywords.select)) {
                // Again, check whether it's a subquery

                int closingBracketIdx = FindCorrespondingClosingBracket.find(rawQuery, i, '(');
                String subQueryInFromStatement = rawQuery.substring(i, closingBracketIdx + 1); // subquery is taken without an alias!
                resultQuery.addFromSource(new Source((new QueryParser()).parseQuery(subQueryInFromStatement.substring(1, subQueryInFromStatement.length() - 1), false)));
                i = closingBracketIdx + 2;

                // Count an alias
                while (i < rawQuery.length() && rawQuery.charAt(i) != ',') {
                    i++;
                }
            }
        }

        return i + 1;
    }

    private int extractJoinTables(int startIdx) {
        return -1;
    }

}
