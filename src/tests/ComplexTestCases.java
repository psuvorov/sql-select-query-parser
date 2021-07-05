package tests;

import com.newjob.parser.QueryParser;
import com.newjob.parser.domain.Query;
import com.newjob.parser.exceptions.InvalidQueryFormatException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ComplexTestCases {

    @Test
    public void query01() throws InvalidQueryFormatException {
        // Arrange
        final String query = "select * from Boards\n" +
                "  left join Lists ON Boards.Id = Lists.BoardId\n" +
                "  inner join (SELECT meta_value As Prenom, post_id FROM wp_postmeta " +
                "                       right join Tab5 on t1 = t2 where id = 7) AS a ON wp_woocommerce_order_items.order_id = a.post_id\n" +
                "  full join Cards ON Users.Id = Cards.LastModifiedById" +
                "  where SalesPersonID IN (SELECT SalesPerson.BusinessEntityID\n" +
                "               FROM   sales.SalesPerson\n" +
                "               WHERE  SalesYTD > 3000000\n" +
                "                      AND SalesOrderHeader.SalesPersonID \n" +
                "                        = Sales.SalesPerson.BusinessEntityID)";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(1, res.getFromSources().size());
        assertEquals(3, res.getJoins().size());

        assertEquals(1, res.getJoins().get(1).getReferencedSubquery().getFromSources().size());

        // TODO: continue to break up 'where' section
    }

    @Test
    public void query02() throws InvalidQueryFormatException {
        // TODO: fails

        // Arrange
        final String query = "" +
                "SELECT Products.*" +
                "FROM Products" +
                "     INNER JOIN " +
                "(" +
                "    SELECT ProductID, Sum(Quantity) as QuantitySum" +
                "    from" +
                "    (" +
                "        SELECT ProductID, Quantity  " +
                "        FROM BasketItems  " +
                "    ) v" +
                "    GROUP BY ProductID" +
                ") ProductTotals" +
                "    ON Products.ID = ProductTotals.ProductID" +
                "ORDER BY QuantitySum DESC";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert


    }



}
