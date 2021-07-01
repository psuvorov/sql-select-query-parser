package com.newjob.parser.domain.terms;

import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.TermType;

public class Source {

    private final TermType type;

    private String simpleSourceTableName = "";

    private Query subQuery;

    private String alias;

    public Source(String simpleSourceTableName, String alias) {
        type = TermType.SimpleTerm;
        this.simpleSourceTableName = simpleSourceTableName;
        this.alias = alias;
    }

    public Source(Query subQuery, String alias) {
        type = TermType.SubQuery;
        this.subQuery = subQuery;
        this.alias = alias;
    }

    public TermType getType() {
        return type;
    }

    public String getSimpleSourceTableName() {
        return simpleSourceTableName;
    }

    public Query getSubQuery() {
        return subQuery;
    }

    public String getAlias() {
        return alias;
    }

}

