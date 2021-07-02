package tests;

import com.newjob.parser.QueryParser;
import com.newjob.parser.domain.Query;
import com.newjob.parser.exceptions.InvalidQueryFormatException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QueryWithGroupByAndHavingTestCases {

    @Test
    public void query01() throws InvalidQueryFormatException {
        // Arrange
        final String query = "SELECT model, COUNT(model) AS Qty_model, " +
                "   AVG(price) AS Avg_price" +
                " FROM PC" +
                " GROUP BY model" +
                " HAVING AVG(price) < 800;";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(1, res.getGroupByColumns().size());
        assertEquals("model", res.getGroupByColumns().get(0));

        assertEquals("AVG(price) < 800", res.getHavingSection().getHavingClause());
    }

    @Test
    public void query02() throws InvalidQueryFormatException {
        // Arrange
        final String query = "SELECT BillingDate, " +
                "       COUNT(*) AS BillingQty, " +
                "       SUM(BillingTotal) AS BillingSum " +
                "FROM Billings " +
                "WHERE BillingDate BETWEEN '2002-05-01' AND '2002-05-31' " +
                "GROUP BY BillingDate " +
                "HAVING COUNT(*) > 1 AND SUM(BillingTotal) > 100 " +
                "limit 10 offset 3";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals("BillingDate BETWEEN '2002-05-01' AND '2002-05-31'", res.getWhereSection().getWhereClause());

        assertEquals(1, res.getGroupByColumns().size());
        assertEquals("BillingDate", res.getGroupByColumns().get(0));

        assertEquals("COUNT(*) > 1 AND SUM(BillingTotal) > 100", res.getHavingSection().getHavingClause());
    }


}
