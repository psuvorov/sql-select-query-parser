package tests;

import com.newjob.parser.QueryParser;
import com.newjob.parser.domain.Query;
import com.newjob.parser.exceptions.InvalidQueryFormatException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WhereTestCases {

    @Test
    public void query01() throws InvalidQueryFormatException {
        // Assert
        String query = "select t.* from Tags as t, Bags b where t.x > 100";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Arrange
        assertEquals("t.x > 100", res.getWhereSection().getWhereClause());
    }

    @Test
    public void query02() throws InvalidQueryFormatException {
        // Assert
        String query = "select t.* from Tags as t, Bags b where t.x > 100 and b.y = 20 limit 3";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Arrange
        assertEquals("t.x > 100 and b.y = 20", res.getWhereSection().getWhereClause());
    }

}
