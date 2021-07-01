package com.newjob.parser.domain.terms;

import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.TermType;

public class Column {

    private final TermType type;

    private String simpleColumnTermName = "";

    private Query subQuery;

    private String alias;

    public Column(String simpleColumnTermName, String alias) {
        type = TermType.SimpleTerm;
        this.simpleColumnTermName = simpleColumnTermName;
        this.alias = alias;
    }

    public Column(Query subQuery, String alias) {
        type = TermType.SubQuery;
        this.subQuery = subQuery;
        this.alias = alias;
    }

    public TermType getType() {
        return type;
    }

    public String getSimpleColumnTermName() {
        return simpleColumnTermName;
    }

    public Query getSubQuery() {
        return subQuery;
    }

    public String getAlias() {
        return alias;
    }
}
