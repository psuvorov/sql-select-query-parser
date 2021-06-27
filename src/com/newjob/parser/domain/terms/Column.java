package com.newjob.parser.domain.terms;

import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.TermType;

public class Column {
    private final TermType type;

    private String simpleColumnTermName = "";

    private Query subQuery;

    public Column(String simpleColumnTermName) {
        type = TermType.SimpleTerm;
        this.simpleColumnTermName = simpleColumnTermName;
    }

    public Column(Query subQuery) {
        type = TermType.SubQuery;
        this.subQuery = subQuery;
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
}
