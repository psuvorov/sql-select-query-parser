package com.newjob.parser.domain.terms;

public class Where {

    private final String whereClause;

    public Where(String whereClause) {
        this.whereClause = whereClause;
    }

    public String getWhereClause() {
        return whereClause;
    }
}
