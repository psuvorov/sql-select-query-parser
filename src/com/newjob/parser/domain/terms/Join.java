package com.newjob.parser.domain.terms;

import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.JoinType;
import com.newjob.parser.domain.enums.TermType;

public class Join {

    private final JoinType joinType;

    private String joinRaw;

    private String referencedTableName = "";

    private Query referencedSubquery;

    private final TermType referencedTermType;

    private final String joinClause;

    public Join(JoinType joinType, String joinRaw) {
        this.joinType = joinType;
        this.joinRaw = joinRaw;
        referencedTermType = TermType.SimpleTerm;
        joinClause = "";
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public TermType getReferencedTermType() {
        return referencedTermType;
    }

    public String getReferencedTableName() {
        return referencedTableName;
    }

    public Query getReferencedSubquery() {
        return referencedSubquery;
    }

    public String getJoinClause() {
        return joinClause;
    }
}
