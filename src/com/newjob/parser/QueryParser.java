package com.newjob.parser;

import com.newjob.parser.algo.FindCorrespondingClosingBracketIndex;
import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.JoinType;
import com.newjob.parser.domain.terms.*;
import com.newjob.parser.exceptions.InvalidQueryFormatException;

import java.util.Locale;

import static com.newjob.parser.Utils.AliasRefinerRegExPattern;

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
                .replaceAll("\\s+", " ") // whitespaces to the single one
                .replace("--", "") // get rid off all comments
                .replace("( ", "(") // proper brackets position
                .replace(" )", ")")
                .replace(";", "");

        replaceSingleJoinWithInnerJoinKeyword();
    }

    // To improve quality of life, turn 'join' to 'inner join' to make parsing process a bit simpler
    private void replaceSingleJoinWithInnerJoinKeyword() {
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

        rawQuery = sb.toString();
    }

    private void validate() throws InvalidQueryFormatException {
        int selectIdx = rawQuery.toLowerCase(Locale.ROOT).indexOf(Keywords.select + " ");
        if (selectIdx == -1)
            throw new InvalidQueryFormatException("Query doesn't contain 'select' statement or the syntax nearby is wrong");

        int fromIdx = rawQuery.toLowerCase(Locale.ROOT).indexOf(" " + Keywords.from + " ");
        if (fromIdx == -1)
            throw new InvalidQueryFormatException("Query doesn't contain 'from' statement or the syntax nearby is wrong");

        // TODO: think about a random sequence of brackets in an alias...
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
            nextStatementStartIdx = extractOrderByStatements(nextStatementStartIdx);
        }

        if (nextKeywordAfterSourceSection.equalsIgnoreCase(Keywords.limit)) {
            nextStatementStartIdx = extractLimitStatement(nextStatementStartIdx);
        }

        if (nextKeywordAfterSourceSection.equalsIgnoreCase(Keywords.offset)) {
            nextStatementStartIdx = extractOffsetStatement(nextStatementStartIdx);
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
            if (columnRaw.toLowerCase(Locale.ROOT).startsWith("(select")) {

                int closingBracketIdx = FindCorrespondingClosingBracketIndex.find(columnRaw, 0, '(');
                if (closingBracketIdx == columnRaw.length() - 1) {
                    // Column has no an alias
                    resultQuery.addColumn(new Column((new QueryParser()).parseQuery(columnRaw.substring(1, columnRaw.length() - 1), false), ""));
                } else {
                    String alias = columnRaw
                            .substring(closingBracketIdx + 1)
                            .replaceAll(AliasRefinerRegExPattern, "")
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
                    String columnAlias = columnRaw.substring(aliasSplitterIdx).replaceAll(AliasRefinerRegExPattern, "").trim();
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
            } else if (rawQuery.charAt(j) == '-' && rawQuery.charAt(j + 1) == '-') { // query end terminator
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
            if (sourceRaw.toLowerCase(Locale.ROOT).startsWith("(select")) {

                int closingBracketIdx = FindCorrespondingClosingBracketIndex.find(sourceRaw, 0, '(');
                if (closingBracketIdx == sourceRaw.length() - 1) {
                    // Column has no an alias
                    resultQuery.addFromSource(new Source((new QueryParser()).parseQuery(sourceRaw.substring(1, sourceRaw.length() - 1), false), ""));
                } else {
                    String alias = sourceRaw
                            .substring(closingBracketIdx + 1)
                            .replaceAll(AliasRefinerRegExPattern, "")
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
                    String sourceAlias = sourceRaw.substring(aliasSplitterIdx).trim().replaceAll(AliasRefinerRegExPattern, "");
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
                    String wholeJoinStatement = getWholeJoinStatement(j).trim();
                    addExtractedJoinStatements(JoinType.Inner, Keywords.innerJoin, wholeJoinStatement);
                    i += wholeJoinStatement.length() - 2;
                    break;
                } else if (j + Keywords.leftJoin.length() < rawQuery.length() && rawQuery.substring(j, j + Keywords.leftJoin.length()).equalsIgnoreCase(Keywords.leftJoin)) {
                    String wholeJoinStatement = getWholeJoinStatement(j).trim();
                    addExtractedJoinStatements(JoinType.Left, Keywords.leftJoin, wholeJoinStatement);
                    i += wholeJoinStatement.length() - 2;
                    break;
                } else if (j + Keywords.rightJoin.length() < rawQuery.length() && rawQuery.substring(j, j + Keywords.rightJoin.length()).equalsIgnoreCase(Keywords.rightJoin)) {
                    String wholeJoinStatement = getWholeJoinStatement(j).trim();
                    addExtractedJoinStatements(JoinType.Right, Keywords.rightJoin, wholeJoinStatement);
                    i += wholeJoinStatement.length() - 2;
                    break;
                } else if (j + Keywords.fullJoin.length() < rawQuery.length() && rawQuery.substring(j, j + Keywords.fullJoin.length()).equalsIgnoreCase(Keywords.fullJoin)) {
                    String wholeJoinStatement = getWholeJoinStatement(j).trim();
                    addExtractedJoinStatements(JoinType.Full, Keywords.fullJoin, wholeJoinStatement);
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

    private String getWholeJoinStatement(int startIdx) {
        StringBuilder sb = new StringBuilder();
        sb.append(rawQuery.charAt(startIdx++));

        int j = startIdx;
        while (j < rawQuery.length()) {
            // If we bump into the next keyword, stop here
            if (
                 rawQuery.substring(j, j + Keywords.innerJoin.length()).equalsIgnoreCase(Keywords.innerJoin) ||
                 rawQuery.substring(j, j + Keywords.leftJoin.length()).equalsIgnoreCase(Keywords.leftJoin) ||
                 rawQuery.substring(j, j + Keywords.rightJoin.length()).equalsIgnoreCase(Keywords.rightJoin) ||
                 rawQuery.substring(j, j + Keywords.fullJoin.length()).equalsIgnoreCase(Keywords.fullJoin) ||
                 rawQuery.substring(j, j + Keywords.where.length()).equalsIgnoreCase(Keywords.where) ||
                 rawQuery.substring(j, j + Keywords.groupBy.length()).equalsIgnoreCase(Keywords.groupBy) ||
                 rawQuery.substring(j, j + Keywords.having.length()).equalsIgnoreCase(Keywords.having) ||
                 rawQuery.substring(j, j + Keywords.orderBy.length()).equalsIgnoreCase(Keywords.orderBy) ||
                 rawQuery.substring(j, j + Keywords.limit.length()).equalsIgnoreCase(Keywords.limit) ||
                 rawQuery.substring(j, j + Keywords.offset.length()).equalsIgnoreCase(Keywords.offset)
            ) {
                break;
            } else if (rawQuery.charAt(j) == '-' && rawQuery.charAt(j + 1) == '-') { // query end terminator
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

    private void addExtractedJoinStatements(JoinType joinType, String joinKeyword, String wholeJoinStatement) {
        String joinStatementWithoutJoin = wholeJoinStatement.substring(joinKeyword.length() + 1);

        if (joinStatementWithoutJoin.toLowerCase(Locale.ROOT).startsWith("(select")) {
            // We've got a subquery
            try {
                addDataFromJoinWithSubquery(joinType, joinStatementWithoutJoin);
            } catch (InvalidQueryFormatException e) {
                // TODO: !!
                e.printStackTrace();
                System.exit(0);
            }
        } else {
            addDataFromJoin(joinType, joinStatementWithoutJoin);
        }
    }

    private void addDataFromJoinWithSubquery(JoinType joinType, String joinStatementWithoutJoin) throws InvalidQueryFormatException {
        int closingBracketIdx = FindCorrespondingClosingBracketIndex.find(joinStatementWithoutJoin, 0, '(');
        String subquery = joinStatementWithoutJoin.substring(1, closingBracketIdx);

        String joinStatementWithoutSubquery = joinStatementWithoutJoin.substring(subquery.length() + 2);

        String[] restResJoinItems = addRestResJoinItems(joinStatementWithoutSubquery, true);
        resultQuery.addJoin(new Join(joinType, new QueryParser().parseQuery(subquery, false), restResJoinItems[1], restResJoinItems[2]));
    }

    private void addDataFromJoin(JoinType joinType, String joinStatementWithoutJoin) {
        String[] restResJoinItems = addRestResJoinItems(joinStatementWithoutJoin, false);
        resultQuery.addJoin(new Join(joinType, restResJoinItems[0], restResJoinItems[1], restResJoinItems[2]));
    }

    private String[] addRestResJoinItems(String stringToExtract, boolean isSubquery) {
        String[] tokens = stringToExtract.trim().split(" ");

        String[] res = new String[3]; // Referenced Table Name, Alias, Join clause
        res[0] = "";
        res[1] = "";
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("on")) {
                if (isSubquery) {
                    if (tokens[0].equalsIgnoreCase("as")) {
                        // AS table_alias
                        res[1] = tokens[1];
                    } else {
                        // table_alias
                        res[1] = tokens[0];
                    }
                } else {
                    if (tokens[1].equalsIgnoreCase("as")) {
                        // table_name as table_alias
                        res[0] = tokens[0];
                        res[1] = tokens[2];
                    } else if (i == 2) {
                        // table_name table_alias
                        res[0] = tokens[0];
                        res[1] = tokens[1];
                    } else {
                        // table_name
                        res[0] = tokens[0];
                    }
                }

                // Add Join clause
                StringBuilder joinClause = new StringBuilder();
                for (int j = i + 1; j < tokens.length; j++) {
                    joinClause.append(tokens[j]).append(" ");
                }
                joinClause.delete(joinClause.length() - 1, joinClause.length());
                res[2] = joinClause.toString();
            }
        }

        return res;
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
            } else if (rawQuery.charAt(j) == '-' && rawQuery.charAt(j + 1) == '-') { // query end terminator
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

        resultQuery.setWhereSection(new Where(sb.delete(0, Keywords.where.length() + 1).toString().trim()));

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
            } else if (rawQuery.charAt(j) == '-' && rawQuery.charAt(j + 1) == '-') { // query end terminator
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
            } else if (rawQuery.charAt(j) == '-' && rawQuery.charAt(j + 1) == '-') { // query end terminator
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

    private int extractOrderByStatements(int startIdx) {
        StringBuilder sb = new StringBuilder();

        int j = startIdx;
        while (j < rawQuery.length()) {

            // The next bunch of keywords that might be following after 'order by' section
            if (j + Keywords.limit.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.limit.length()).equalsIgnoreCase(Keywords.limit)) {
                nextKeywordAfterSourceSection = Keywords.limit;
                break;
            } else if (j + Keywords.offset.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.offset.length()).equalsIgnoreCase(Keywords.offset)) {
                nextKeywordAfterSourceSection = Keywords.offset;
                break;
            } else if (rawQuery.charAt(j) == '-' && rawQuery.charAt(j + 1) == '-') { // query end terminator
                break;
            } else {
                sb.append(rawQuery.charAt(j));
                j++;
            }
        }

        String orderByConditions = sb.delete(0, Keywords.orderBy.length()).toString().trim();

        System.out.println();
//        resultQuery.addSortColumn();

        return j;
    }

    private int extractLimitStatement(int startIdx) {
        StringBuilder sb = new StringBuilder();

        int j = startIdx;
        while (j < rawQuery.length()) {

            // The next bunch of keywords that might be following after 'limit' section
            if (j + Keywords.offset.length() >= rawQuery.length() || rawQuery.substring(j, j + Keywords.offset.length()).equalsIgnoreCase(Keywords.offset)) {
                nextKeywordAfterSourceSection = Keywords.offset;
                break;
            } else if (rawQuery.charAt(j) == '-' && rawQuery.charAt(j + 1) == '-') { // query end terminator
                break;
            } else {
                sb.append(rawQuery.charAt(j));
                j++;
            }
        }

        String limitValue = sb.delete(0, Keywords.limit.length()).toString().trim();
        resultQuery.setLimit(Integer.parseInt(limitValue));

        System.out.println();
//        resultQuery.addSortColumn();

        return j;
    }

    private int extractOffsetStatement(int startIdx) {
        StringBuilder sb = new StringBuilder();

        int j = startIdx;
        while (j < rawQuery.length()) {

            if (rawQuery.charAt(j) == '-' && rawQuery.charAt(j + 1) == '-') { // query end terminator
                break;
            } else {
                sb.append(rawQuery.charAt(j));
                j++;
            }
        }

        String offsetValue = sb.delete(0, Keywords.offset.length()).toString().trim();
        resultQuery.setOffset(Integer.parseInt(offsetValue));

        System.out.println();

        return j;
    }



}
