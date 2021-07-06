package com.newjob.parser.domain;

import com.newjob.parser.domain.enums.TermType;
import com.newjob.parser.domain.terms.*;
import java.util.ArrayList;
import java.util.List;

public class Query {

    private final List<Column> columns = new ArrayList<>();

    private final List<Source> fromSources = new ArrayList<>();

    private final List<Join> joins = new ArrayList<>();

    private Where whereSection;

    private final List<String> groupByColumns = new ArrayList<>();

    private Having havingSection;

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

    // TODO: turn into List<WhereClause>
    public void setWhereSection(Where whereSection) {
        this.whereSection = whereSection;
    }

    public void addGroupByColumn(String groupByColumn) {
        groupByColumns.add(groupByColumn);
    }

    public void setHavingSection(Having havingSection) {
        this.havingSection = havingSection;
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

    public Where getWhereSection() {
        return whereSection;
    }

    public List<String> getGroupByColumns() {
        return groupByColumns;
    }

    public Having getHavingSection() {
        return havingSection;
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

    private String getInfo(int paddingLevel) {
        String spr = System.lineSeparator();

        StringBuilder res = new StringBuilder();
        res.append("\t".repeat(paddingLevel)).append("_____________________________________").append(spr);
        res.append("\t".repeat(paddingLevel)).append("OVERALL QUERY INFO:").append(spr);
        paddingLevel++;

        // --- COLUMNS ---
        res.append("\t".repeat(paddingLevel)).append("COLUMNS:").append(spr);
        for (Column column : this.getColumns()) {
            if (column.getType() == TermType.SimpleTerm) {
                res.append("\t".repeat(paddingLevel + 1)).append(column.getSimpleColumnTermName());
                if (!column.getAlias().isEmpty()) {
                    res.append(" AS ").append(column.getAlias());
                }
                res.append(spr);
            } else {
                Query subQuery = column.getSubQuery();
                res.append(subQuery.getInfo(paddingLevel + 1));
            }
        }

        // --- SOURCES ---
        res.append("\t".repeat(paddingLevel)).append("SOURCES:").append(spr);
        for (Source source : this.getFromSources()) {
            if (source.getType() == TermType.SimpleTerm) {
                res.append("\t".repeat(paddingLevel + 1)).append("Table Name: ").append(source.getSimpleSourceTableName());
                if (!source.getAlias().isEmpty()) {
                    res.append(" AS ").append(source.getAlias());
                }
                res.append(spr);
            } else {
                Query subQuery = source.getSubQuery();
                res.append("\t".repeat(paddingLevel + 1)).append("SubQuery");
                if (!source.getAlias().isEmpty()) {
                    res.append(" AS ").append(source.getAlias());
                }
                res.append(":").append(spr);
                res.append(subQuery.getInfo(paddingLevel + 1));
            }
        }

        // --- JOINS ---
        if (!this.getJoins().isEmpty())
            res.append("\t".repeat(paddingLevel)).append("JOINS:").append(spr);
        for (Join join : this.getJoins()) {
            res.append("\t".repeat(paddingLevel + 1)).append("Join Type: ").append(join.getJoinType()).append(spr);
            if (join.getReferencedTermType() == TermType.SimpleTerm) {
                res.append("\t".repeat(paddingLevel + 1)).append("Referenced Table: ").append(join.getReferencedTableName());
                if (!join.getReferencedAlias().isEmpty()) {
                    res.append(" AS ").append(join.getReferencedAlias());
                }
                res.append(spr).append(spr);
            } else {
                Query subQuery = join.getReferencedSubquery();
                res.append("\t".repeat(paddingLevel + 1)).append("SubQuery");
                if (!join.getReferencedAlias().isEmpty()) {
                    res.append(" AS ").append(join.getReferencedAlias());
                }
                res.append(":").append(spr);
                res.append(subQuery.getInfo(paddingLevel + 1));
            }
        }


        // --- WHERE SECTION ---
        if (this.getWhereSection() != null) {
            res.append("\t".repeat(paddingLevel)).append("WHERE:").append(spr);
            res.append("\t".repeat(paddingLevel + 1)).append(this.getWhereSection().getWhereClause()).append(spr);
        }

        // --- GROUP BY SECTION ---
        if (!this.getGroupByColumns().isEmpty()) {
            res.append("\t".repeat(paddingLevel)).append("GROUP BY:").append(spr);

            for (String groupByColumn : this.getGroupByColumns()) {
                res.append("\t".repeat(paddingLevel + 1)).append(groupByColumn).append(spr);
            }
        }

        // --- HAVING SECTION ---
        if (this.getHavingSection() != null) {
            res.append("\t".repeat(paddingLevel)).append("HAVING:").append(spr);
            res.append("\t".repeat(paddingLevel + 1)).append(this.getHavingSection()).append(spr);
        }

        // --- ORDER BY SECTION ---
        if (!this.getSortColumns().isEmpty()) {
            res.append("\t".repeat(paddingLevel)).append("ORDER BY:").append(spr);
            for (Sort sort : this.getSortColumns()) {
                res.append("\t".repeat(paddingLevel + 1)).append(sort.getColumn()).append(" ").append(sort.getSortType()).append(spr);
            }
        }

        // --- LIMIT AND OFFSET SECTION ---
        if (this.getLimit() != null)
            res.append("\t".repeat(paddingLevel)).append("LIMIT:").append(this.getLimit().intValue()).append(spr);

        if (this.getOffset() != null)
            res.append("\t".repeat(paddingLevel)).append("OFFSET:").append(this.getOffset().intValue()).append(spr);


        res.append("\t".repeat(paddingLevel - 1)).append("-------------------------------------").append(spr);

        return res.toString();
    }

    @Override
    public String toString() {
        return getInfo(0);
    }
}
