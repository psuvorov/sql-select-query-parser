package tests;

import com.newjob.parser.QueryParser;
import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.JoinType;
import com.newjob.parser.domain.enums.SortType;
import com.newjob.parser.domain.enums.TermType;
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
        // Arrange
        final String query =
                "SELECT Products.*\n" +
                "FROM Products\n" +
                "     INNER JOIN \n" +
                "(\n" +
                "    SELECT ProductID, Sum(Quantity) as QuantitySum\n" +
                "    from\n" +
                "    (\n" +
                "        SELECT ProductID, Quantity  \n" +
                "        FROM BasketItems  \n" +
                "    ) v\n" +
                "    GROUP BY ProductID\n" +
                ") ProductTotals\n" +
                "    ON Products.ID = ProductTotals.ProductID\n" +
                "ORDER BY QuantitySum DESC";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals("Products.*", res.getColumns().get(0).getSimpleColumnTermName());
        assertEquals("Products", res.getFromSources().get(0).getSimpleSourceTableName());
        assertEquals(JoinType.Inner, res.getJoins().get(0).getJoinType());
        assertEquals(TermType.SubQuery, res.getJoins().get(0).getReferencedTermType());
        assertEquals("ProductTotals", res.getJoins().get(0).getReferencedAlias());
        assertEquals("Products.ID = ProductTotals.ProductID", res.getJoins().get(0).getJoinClause());
        assertEquals("QuantitySum", res.getSortColumns().get(0).getColumn());
        assertEquals(SortType.Desc, res.getSortColumns().get(0).getSortType());


    }



}
