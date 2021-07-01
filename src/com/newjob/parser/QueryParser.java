package com.newjob.parser;

import com.newjob.parser.algo.BalancedBracketsChecker;
import com.newjob.parser.algo.FindCorrespondingClosingBracketIndex;
import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.JoinType;
import com.newjob.parser.domain.terms.*;
import com.newjob.parser.exceptions.InvalidQueryFormatException;

import java.util.Locale;

public class QueryParser {

    private String rawQuery;

    private final Query resultQuery = new Query();

    private String nextKeywordAfterSourceSection;

    // Select statement tokens
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
        rawQuery += "---------------------------------";

        validate();
        performParsing();

        return resultQuery;
    }

    private void normalize() {
        rawQuery = rawQuery
                .trim()
                .replaceAll("\\s+", " ")
                .replace("-", "")
                .replace("( ", "(")
                .replace(" )", ")");

        replaceSingleJoinToInnerJoinKeyword();
    }

    // To improve quality of life, turn 'join' to 'inner join' to make parsing process a bit simpler
    private void replaceSingleJoinToInnerJoinKeyword() {
        StringBuilder sb = new StringBuilder(rawQuery);

        for (int i = 0; i < sb.length() - Keywords.join.length(); i++) {
            if (sb.substring(i, i + Keywords.join.length()).equalsIgnoreCase(Keywords.join)) {

                String possibleInnerJoinStatement = sb.substring(i - Keywords.innerJoin.length() + Keywords.join.length(), i + Keywords.join.length());
                String possibleLeftJoinStatement = sb.substring(i - Keywords.leftJoin.length() + Keywords.join.length(), i + Keywords.join.length());
                String possibleRightJoinStatement = sb.substring(i - Keywords.rightJoin.length() + Keywords.join.length(), i + Keywords.join.length());
                String possibleFullJoinStatement = sb.substring(i - Keywords.fullJoin.length() + Keywords.join.length(), i + Keywords.join.length());

                if (!(possibleInnerJoinStatement.equalsIgnoreCase(Keywords.innerJoin) || possibleLeftJoinStatement.equalsIgnoreCase(Keywords.leftJoin) ||
                      possibleRightJoinStatement.equalsIgnoreCase(Keywords.rightJoin) || possibleFullJoinStatement.equalsIgnoreCase(Keywords.fullJoin))) {
                    sb.insert(i, "inner ", 0, 6 /* 'inner ' length */);
                }
            }
        }
    }

    private void validate() throws InvalidQueryFormatException {
        int selectIdx = rawQuery.toLowerCase(Locale.ROOT).indexOf(Keywords.select + " ");
        if (selectIdx == -1)
            throw new InvalidQueryFormatException("Query doesn't contain 'select' statement or the syntax nearby is wrong");

        int fromIdx = rawQuery.toLowerCase(Locale.ROOT).indexOf(" " + Keywords.from + " ");
        if (fromIdx == -1)
            throw new InvalidQueryFormatException("Query doesn't contain 'from' statement or the syntax nearby is wrong");

//        if (!BalancedBracketsChecker.isBalanced(rawQuery))
//            throw new InvalidQueryFormatException();
    }

    private void performParsing() throws InvalidQueryFormatException {
        int startIdx = Keywords.select.length() + 1;

        int sourceStartIdx = extractColumns(startIdx);
        int nextStatementStartIdx = extractSources(sourceStartIdx + Keywords.from.length() + 1);

        // In case it's a simple query (with no keywords after 'from' term, please stop here)
        if (nextStatementStartIdx >= rawQuery.length() || nextKeywordAfterSourceSection == null)
            return;

        // The next possible keyword order is:
        // - join stuff
        // - where
        // - group by
        // - having
        // - order by
        // - limit
        // - offset

        if (nextKeywordAfterSourceSection.equalsIgnoreCase(Keywords.innerJoin) ||
            nextKeywordAfterSourceSection.equalsIgnoreCase(Keywords.leftJoin) ||
            nextKeywordAfterSourceSection.equalsIgnoreCase(Keywords.rightJoin) ||
            nextKeywordAfterSourceSection.equalsIgnoreCase(Keywords.fullJoin)
        ) {
            nextStatementStartIdx = extractJoinStatements(nextStatementStartIdx - nextKeywordAfterSourceSection.length() - 1);
        }

        if (nextKeywordAfterSourceSection.equalsIgnoreCase(Keywords.where)) {
            nextStatementStartIdx = extractWhereStatements(nextStatementStartIdx);
        }

        if (nextKeywordAfterSourceSection.equalsIgnoreCase(Keywords.groupBy)) {
            nextStatementStartIdx = extractGroupByStatements(nextStatementStartIdx);
        }

        if (nextKeywordAfterSourceSection.equalsIgnoreCase(Keywords.having)) {
            nextStatementStartIdx = extractHavingStatements(nextStatementStartIdx);
        }

        if (nextKeywordAfterSourceSection.equalsIgnoreCase(Keywords.orderBy)) {

        }

        if (nextKeywordAfterSourceSection.equalsIgnoreCase(Keywords.limit)) {

        }

        if (nextKeywordAfterSourceSection.equalsIgnoreCase(Keywords.offset)) {

        }


        System.out.println();

    }

    // Find all select members respective to the current level select statement
    private int extractColumns(int startIdx) throws InvalidQueryFormatException {
        StringBuilder sb = new StringBuilder();

        int j = startIdx;
        while (j < rawQuery.length()) {

            // The end of 'from' section, collect gathered data
            if (j + Keywords.from.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.from.length()).equalsIgnoreCase(Keywords.from)) {
                addExtractedColumnsToResultQuery(sb.toString());
                return j;
            } else if (rawQuery.charAt(j) == '(') {
                int closingBracketIdx = FindCorrespondingClosingBracketIndex.find(rawQuery, j, '(');
                String whateverInBrackets = rawQuery.substring(j, closingBracketIdx + 1); // this can be just grouping by brackets or a subquery. Take it as is at this point.
                sb.append(whateverInBrackets);
                j += whateverInBrackets.length();
            } else {
                sb.append(rawQuery.charAt(j));
                j++;
            }
        }

        throw new InvalidQueryFormatException("Malformed query, 'select' keyword is missing");
    }

    // TODO: two functions addExtractedSourcesToResultQuery and addExtractedColumnsToResultQuery are very similar to each other
    private void addExtractedColumnsToResultQuery(String columnsRaw) throws InvalidQueryFormatException {
        String[] splitColumnsRaw = columnsRaw.split(", ");

        for (String columnRaw : splitColumnsRaw) {
            columnRaw = columnRaw.trim();

            // Extracted column can be a subquery, let's handle that
            if (columnRaw.startsWith("(select")) {

                int closingBracketIdx = FindCorrespondingClosingBracketIndex.find(columnRaw, 0, '(');
                if (closingBracketIdx == columnRaw.length() - 1) {
                    // Column has no an alias
                    resultQuery.addColumn(new Column((new QueryParser()).parseQuery(columnRaw.substring(1, columnRaw.length() - 1), false), ""));
                } else {
                    String alias = columnRaw
                            .substring(closingBracketIdx + 1)
                            .replaceAll("((?i) as )|(')|(\")", "")
                            .trim();

                    resultQuery.addColumn(new Column((new QueryParser()).parseQuery(columnRaw.substring(1, closingBracketIdx), false), alias));
                }
            } else {
                int aliasSplitterIdx = columnRaw.toLowerCase(Locale.ROOT).indexOf(" as ");

                if (aliasSplitterIdx == -1)
                    aliasSplitterIdx = columnRaw.toLowerCase(Locale.ROOT).indexOf(" ");

                if (aliasSplitterIdx == -1) {
                    resultQuery.addColumn(new Column(columnRaw, ""));
                } else {
                    String columnWithoutAlias = columnRaw.substring(0, aliasSplitterIdx).trim();
                    String columnAlias = columnRaw.substring(aliasSplitterIdx).trim().replaceAll("((?i)as )|(')|(\")", "");
                    resultQuery.addColumn(new Column(columnWithoutAlias, columnAlias));
                }
            }
        }
    }

    private int extractSources(int startIdx) throws InvalidQueryFormatException {
        StringBuilder sb = new StringBuilder();

        int j = startIdx;
        while (j < rawQuery.length()) {

            // If we bump into 'inner join' | 'left join' | 'right join' | 'full join' | 'group by' | 'having' | 'where' | 'limit' | 'offset',
            // then it's the end of sources declaration
            if (j + Keywords.innerJoin.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.innerJoin.length()).equalsIgnoreCase(Keywords.innerJoin)) {
                addExtractedSourcesToResultQuery(sb.toString());
                nextKeywordAfterSourceSection = Keywords.innerJoin;
                return j;
            } else if (j + Keywords.leftJoin.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.leftJoin.length()).equalsIgnoreCase(Keywords.leftJoin)) {
                addExtractedSourcesToResultQuery(sb.toString());
                nextKeywordAfterSourceSection = Keywords.leftJoin;
                return j;
            } else if (j + Keywords.rightJoin.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.rightJoin.length()).equalsIgnoreCase(Keywords.rightJoin)) {
                addExtractedSourcesToResultQuery(sb.toString());
                nextKeywordAfterSourceSection = Keywords.rightJoin;
                return j;
            } else if (j + Keywords.fullJoin.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.fullJoin.length()).equalsIgnoreCase(Keywords.fullJoin)) {
                addExtractedSourcesToResultQuery(sb.toString());
                nextKeywordAfterSourceSection = Keywords.fullJoin;
                return j;
            } else if (j + Keywords.groupBy.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.groupBy.length()).equalsIgnoreCase(Keywords.groupBy)) {
                addExtractedSourcesToResultQuery(sb.toString());
                nextKeywordAfterSourceSection = Keywords.groupBy;
                return j;
            } else if (j + Keywords.having.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.having.length()).equalsIgnoreCase(Keywords.having)) {
                addExtractedSourcesToResultQuery(sb.toString());
                nextKeywordAfterSourceSection = Keywords.having;
                return j;
            } else if (j + Keywords.where.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.where.length()).equalsIgnoreCase(Keywords.where)) {
                addExtractedSourcesToResultQuery(sb.toString());
                nextKeywordAfterSourceSection = Keywords.where;
                return j;
            } else if (j + Keywords.limit.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.limit.length()).equalsIgnoreCase(Keywords.limit)) {
                addExtractedSourcesToResultQuery(sb.toString());
                nextKeywordAfterSourceSection = Keywords.limit;
                return j;
            } else if (j + Keywords.offset.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.offset.length()).equalsIgnoreCase(Keywords.offset)) {
                addExtractedSourcesToResultQuery(sb.toString());
                nextKeywordAfterSourceSection = Keywords.offset;
                return j;
            } else if (rawQuery.charAt(j) == '(') {
                int closingBracketIdx = FindCorrespondingClosingBracketIndex.find(rawQuery, j, '(');
                String whateverInBrackets = rawQuery.substring(j, closingBracketIdx + 1); // this can be just grouping by brackets or a subquery. Take it as is at this point.
                sb.append(whateverInBrackets);
                j += whateverInBrackets.length();
            } else if (rawQuery.charAt(j) == '-') { // query end terminator
                addExtractedSourcesToResultQuery(sb.toString());
                // no nextKeywordAfterSourceSection, this is the end of the query text
                return j;
            } else {
                sb.append(rawQuery.charAt(j));
                j++;
            }
        }

        throw new InvalidQueryFormatException("Malformed query, 'from' keyword is missing");
    }

    // TODO: two functions addExtractedSourcesToResultQuery and addExtractedColumnsToResultQuery are very similar to each other
    private void addExtractedSourcesToResultQuery(String sourcesRaw) throws InvalidQueryFormatException {
        String[] splitSourcesRaw = sourcesRaw.split(", ");

        for (String sourceRaw : splitSourcesRaw) {
            sourceRaw = sourceRaw.trim();

            // Extracted source can be a subquery, let's handle that
            if (sourceRaw.startsWith("(select")) {

                int closingBracketIdx = FindCorrespondingClosingBracketIndex.find(sourceRaw, 0, '(');
                if (closingBracketIdx == sourceRaw.length() - 1) {
                    // Column has no an alias
                    resultQuery.addFromSource(new Source((new QueryParser()).parseQuery(sourceRaw.substring(1, sourceRaw.length() - 1), false), ""));
                } else {
                    String alias = sourceRaw
                            .substring(closingBracketIdx + 1)
                            .replaceAll("((?i) as )|(')|(\")", "")
                            .trim();

                    String ss = sourceRaw.substring(1, closingBracketIdx);
                    resultQuery.addFromSource(new Source((new QueryParser()).parseQuery(ss, false), alias));
                }
            } else {
                int aliasSplitterIdx = sourceRaw.toLowerCase(Locale.ROOT).indexOf(" as ");

                if (aliasSplitterIdx == -1)
                    aliasSplitterIdx = sourceRaw.toLowerCase(Locale.ROOT).indexOf(" ");

                if (aliasSplitterIdx == -1) {
                    resultQuery.addFromSource(new Source(sourceRaw, ""));
                } else {
                    String sourceWithoutAlias = sourceRaw.substring(0, aliasSplitterIdx).trim();
                    String sourceAlias = sourceRaw.substring(aliasSplitterIdx).trim().replaceAll("((?i)as )|(')|(\")", "");
                    resultQuery.addFromSource(new Source(sourceWithoutAlias, sourceAlias));
                }
            }
        }
    }

    private int extractJoinStatements(int startIdx) {
        int i;
        for (i = startIdx; i < rawQuery.length(); i++) {
            char c = rawQuery.charAt(i);
            if (c == ' ')
                continue;

            int j = i;

            while (j < rawQuery.length()) {
                if (j + Keywords.innerJoin.length() < rawQuery.length() && rawQuery.substring(j, j + Keywords.innerJoin.length()).equalsIgnoreCase(Keywords.innerJoin)) {
                    String wholeJoinStatement = getWholeJoinStatement(Keywords.innerJoin, j);
                    resultQuery.addJoin(new Join(JoinType.Inner, wholeJoinStatement));
                    i += wholeJoinStatement.length() - 2;
                    break;
                } else if (j + Keywords.leftJoin.length() < rawQuery.length() && rawQuery.substring(j, j + Keywords.leftJoin.length()).equalsIgnoreCase(Keywords.leftJoin)) {
                    String wholeJoinStatement = getWholeJoinStatement(Keywords.leftJoin, j);
                    resultQuery.addJoin(new Join(JoinType.Left, wholeJoinStatement));
                    i += wholeJoinStatement.length() - 2;
                    break;
                } else if (j + Keywords.rightJoin.length() < rawQuery.length() && rawQuery.substring(j, j + Keywords.rightJoin.length()).equalsIgnoreCase(Keywords.rightJoin)) {
                    String wholeJoinStatement = getWholeJoinStatement(Keywords.rightJoin, j);
                    resultQuery.addJoin(new Join(JoinType.Right, wholeJoinStatement));
                    i += wholeJoinStatement.length() - 2;
                    break;
                } else if (j + Keywords.fullJoin.length() < rawQuery.length() && rawQuery.substring(j, j + Keywords.fullJoin.length()).equalsIgnoreCase(Keywords.fullJoin)) {
                    String wholeJoinStatement = getWholeJoinStatement(Keywords.fullJoin, j);
                    resultQuery.addJoin(new Join(JoinType.Full, wholeJoinStatement));
                    i += wholeJoinStatement.length() - 2;
                    break;
                }
                // Next, let's figure out what the next section is after join(s)
                else if (j + Keywords.where.length() < rawQuery.length() && rawQuery.substring(j, j + Keywords.where.length()).equalsIgnoreCase(Keywords.where)) {
                    nextKeywordAfterSourceSection = Keywords.where;
                    return i;
                } else if (j + Keywords.groupBy.length() < rawQuery.length() && rawQuery.substring(j, j + Keywords.groupBy.length()).equalsIgnoreCase(Keywords.groupBy)) {
                    nextKeywordAfterSourceSection = Keywords.groupBy;
                    return i;
                } else if (j + Keywords.having.length() < rawQuery.length() && rawQuery.substring(j, j + Keywords.having.length()).equalsIgnoreCase(Keywords.having)) {
                    nextKeywordAfterSourceSection = Keywords.having;
                    return i;
                } else if (j + Keywords.orderBy.length() < rawQuery.length() && rawQuery.substring(j, j + Keywords.orderBy.length()).equalsIgnoreCase(Keywords.orderBy)) {
                    nextKeywordAfterSourceSection = Keywords.orderBy;
                    return i;
                } else if (j + Keywords.limit.length() < rawQuery.length() && rawQuery.substring(j, j + Keywords.limit.length()).equalsIgnoreCase(Keywords.limit)) {
                    nextKeywordAfterSourceSection = Keywords.limit;
                    return i;
                } else if (j + Keywords.offset.length() < rawQuery.length() && rawQuery.substring(j, j + Keywords.offset.length()).equalsIgnoreCase(Keywords.offset)) {
                    nextKeywordAfterSourceSection = Keywords.offset;
                    return i;
                } else {
                    j++;
                }
            }
        }

        return i;
    }

    private String getWholeJoinStatement(String joinKeyword, int startIdx) {
        StringBuilder sb = new StringBuilder();

        int j = startIdx;
        while (j < rawQuery.length()) {
            if (
                 j + Keywords.innerJoin.length() >= rawQuery.length() || !joinKeyword.equalsIgnoreCase(Keywords.innerJoin) && rawQuery.substring(j, j + Keywords.innerJoin.length()).equalsIgnoreCase(Keywords.innerJoin) ||
                 j + Keywords.leftJoin.length() >= rawQuery.length() || !joinKeyword.equalsIgnoreCase(Keywords.leftJoin) && rawQuery.substring(j, j + Keywords.leftJoin.length()).equalsIgnoreCase(Keywords.leftJoin) ||
                 j + Keywords.rightJoin.length() >= rawQuery.length() || !joinKeyword.equalsIgnoreCase(Keywords.rightJoin) && rawQuery.substring(j, j + Keywords.rightJoin.length()).equalsIgnoreCase(Keywords.rightJoin) ||
                 j + Keywords.fullJoin.length() >= rawQuery.length() || !joinKeyword.equalsIgnoreCase(Keywords.fullJoin) && rawQuery.substring(j, j + Keywords.fullJoin.length()).equalsIgnoreCase(Keywords.fullJoin) ||
                 j + Keywords.where.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.where.length()).equalsIgnoreCase(Keywords.where) ||
                 j + Keywords.groupBy.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.groupBy.length()).equalsIgnoreCase(Keywords.groupBy) ||
                 j + Keywords.having.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.having.length()).equalsIgnoreCase(Keywords.having) ||
                 j + Keywords.orderBy.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.orderBy.length()).equalsIgnoreCase(Keywords.orderBy) ||
                 j + Keywords.limit.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.limit.length()).equalsIgnoreCase(Keywords.limit) ||
                 j + Keywords.offset.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.offset.length()).equalsIgnoreCase(Keywords.offset)
            ) {
                break;
            } else if (rawQuery.charAt(j) == '-') { // query end terminator
                break;
            } else if (rawQuery.charAt(j) == '(') {
                int closingBracketIdx = FindCorrespondingClosingBracketIndex.find(rawQuery, j, '(');
                String whateverInBrackets = rawQuery.substring(j, closingBracketIdx + 1); // this can be just grouping by brackets or a subquery. Take it as is at this point.
                sb.append(whateverInBrackets);
                j += whateverInBrackets.length();
            } else {
                sb.append(rawQuery.charAt(j));
                j++;
            }
        }

        return sb.toString();
    }

    private int extractWhereStatements(int startIdx) {
        StringBuilder sb = new StringBuilder();

        int j = startIdx;
        while (j < rawQuery.length()) {

            // The next bunch of keywords that might be following after 'where' section
            if (j + Keywords.groupBy.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.groupBy.length()).equalsIgnoreCase(Keywords.groupBy)) {
                nextKeywordAfterSourceSection = Keywords.groupBy;
                break;
            } else if (j + Keywords.having.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.having.length()).equalsIgnoreCase(Keywords.having)) {
                nextKeywordAfterSourceSection = Keywords.having;
                break;
            } else if (j + Keywords.orderBy.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.orderBy.length()).equalsIgnoreCase(Keywords.orderBy)) {
                nextKeywordAfterSourceSection = Keywords.orderBy;
                break;
            } else if (j + Keywords.limit.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.limit.length()).equalsIgnoreCase(Keywords.limit)) {
                nextKeywordAfterSourceSection = Keywords.limit;
                break;
            } else if (j + Keywords.offset.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.offset.length()).equalsIgnoreCase(Keywords.offset)) {
                nextKeywordAfterSourceSection = Keywords.offset;
                break;
            } else if (rawQuery.charAt(j) == '-') { // query end terminator
                break;
            } else if (rawQuery.charAt(j) == '(') {
                int closingBracketIdx = FindCorrespondingClosingBracketIndex.find(rawQuery, j, '(');
                String whateverInBrackets = rawQuery.substring(j, closingBracketIdx + 1); // this can be just grouping by brackets or a subquery. Take it as is at this point.
                sb.append(whateverInBrackets);
                j += whateverInBrackets.length();
            } else {
                sb.append(rawQuery.charAt(j));
                j++;
            }
        }
        resultQuery.setWhereSection(new Where(sb.toString()));

        return j;
    }

    private int extractGroupByStatements(int startIdx) {
        StringBuilder sb = new StringBuilder();

        int j = startIdx;
        while (j < rawQuery.length()) {

            // The next bunch of keywords that might be following after 'group by' section
            if (j + Keywords.having.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.having.length()).equalsIgnoreCase(Keywords.having)) {
                nextKeywordAfterSourceSection = Keywords.having;
                break;
            } else if (j + Keywords.orderBy.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.orderBy.length()).equalsIgnoreCase(Keywords.orderBy)) {
                nextKeywordAfterSourceSection = Keywords.orderBy;
                break;
            } else if (j + Keywords.limit.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.limit.length()).equalsIgnoreCase(Keywords.limit)) {
                nextKeywordAfterSourceSection = Keywords.limit;
                break;
            } else if (j + Keywords.offset.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.offset.length()).equalsIgnoreCase(Keywords.offset)) {
                nextKeywordAfterSourceSection = Keywords.offset;
                break;
            } else if (rawQuery.charAt(j) == '-') { // query end terminator
                break;
            } else {
                sb.append(rawQuery.charAt(j));
                j++;
            }
        }

        String[] columnNames = sb.delete(0, Keywords.groupBy.length()).toString().trim().split(", ");
        for (String columnName : columnNames) {
            resultQuery.addGroupByColumn(columnName);
        }

        return j;
    }

    private int extractHavingStatements(int startIdx) {
        StringBuilder sb = new StringBuilder();

        int j = startIdx;
        while (j < rawQuery.length()) {

            // The next bunch of keywords that might be following after 'having' section
            if (j + Keywords.orderBy.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.orderBy.length()).equalsIgnoreCase(Keywords.orderBy)) {
                nextKeywordAfterSourceSection = Keywords.orderBy;
                break;
            } else if (j + Keywords.limit.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.limit.length()).equalsIgnoreCase(Keywords.limit)) {
                nextKeywordAfterSourceSection = Keywords.limit;
                break;
            } else if (j + Keywords.offset.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.offset.length()).equalsIgnoreCase(Keywords.offset)) {
                nextKeywordAfterSourceSection = Keywords.offset;
                break;
            } else if (rawQuery.charAt(j) == '-') { // query end terminator
                break;
            } else {
                sb.append(rawQuery.charAt(j));
                j++;
            }
        }

        String havingConditions = sb.delete(0, Keywords.having.length()).toString().trim();

        resultQuery.setHavingSection(new Having(havingConditions));

        return j;
    }



}
