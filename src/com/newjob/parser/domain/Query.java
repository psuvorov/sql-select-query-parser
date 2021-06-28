package com.newjob.parser.domain;

import com.newjob.parser.domain.terms.*;
import java.util.ArrayList;
import java.util.List;

public class Query {

    private final List<Column> columns = new ArrayList<>();

    private final List<Source> fromSources = new ArrayList<>();

    private final List<Join> joins = new ArrayList<>();

    private final List<WhereClause> whereClauses = new ArrayList<>();

    private final List<String> groupByColumns = new ArrayList<>();

    private final List<Sort> sortColumns = new ArrayList<>();

    private Integer limit = null;

    private Integer offset = null;

    public void addColumn(Column column) {
        columns.add(column);
    }

    public void addFromSource(Source source) {
        fromSources.add(source);
    }

    public void addJoin(Join join) {
        joins.add(join);
    }

    public void addWhereClause(WhereClause whereClause) {
        whereClauses.add(whereClause);
    }

    public void addGroupByColumn(String groupByColumn) {
        groupByColumns.add(groupByColumn);
    }

    public void addSortColumn(Sort sortColumn) {
        sortColumns.add(sortColumn);
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<Source> getFromSources() {
        return fromSources;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public List<WhereClause> getWhereClauses() {
        return whereClauses;
    }

    public List<String> getGroupByColumns() {
        return groupByColumns;
    }

    public List<Sort> getSortColumns() {
        return sortColumns;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public String getInfo() {
        String spr = System.lineSeparator();

        StringBuilder res = new StringBuilder();
        res.append("-------------------------------------").append(spr);
        res.append("Overall query info:").append(spr);

        res.append("Columns:").append(spr);
//        for (String columnName : this.columns) {
//            res.append("\t").append(columnName);
//        }
        res.append(spr);


        res.append("Limit:").append(spr);
        res.append(limit).append(spr);

        res.append("Offset:").append(spr);
        res.append(offset).append(spr);

        return res.toString();
    }

}
