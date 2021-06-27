package com.newjob.parser.domain.terms;

import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.TermType;

public class Source {

    private final TermType type;

    private String simpleSourceTableName = "";

    private Query subQuery;

    public Source(String simpleSourceTableName) {
        type = TermType.SimpleTerm;
        this.simpleSourceTableName = simpleSourceTableName;
    }

    public Source(Query subQuery) {
        type = TermType.SubQuery;
        this.subQuery = subQuery;
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

}

