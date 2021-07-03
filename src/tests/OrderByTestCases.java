package tests;

import com.newjob.parser.QueryParser;
import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.SortType;
import com.newjob.parser.exceptions.InvalidQueryFormatException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OrderByTestCases {

    @Test
    public void query01() throws InvalidQueryFormatException {
        // Arrange
        final String query = "SELECT ProductID, Name FROM Production.Product  " +
                "   AVG(price) AS Avg_price" +
                " WHERE Name LIKE 'Lock Washer%'" +
                " ORDER BY ProductID;";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(1, res.getSortColumns().size());
        assertEquals("ProductID", res.getSortColumns().get(0).getColumn());
        assertEquals(SortType.Asc, res.getSortColumns().get(0).getSortType());
    }

    @Test
    public void query02() throws InvalidQueryFormatException {
        // Arrange
        final String query = "SELECT ProductID, Name FROM Production.Product  " +
                "   AVG(price) AS Avg_price" +
                " WHERE Name LIKE 'Lock Washer%'" +
                " ORDER BY ProductID, Name desc;";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert
        assertEquals(2, res.getSortColumns().size());
        assertEquals("ProductID", res.getSortColumns().get(0).getColumn());
        assertEquals(SortType.Asc, res.getSortColumns().get(0).getSortType());
        assertEquals("Name", res.getSortColumns().get(1).getColumn());
        assertEquals(SortType.Desc, res.getSortColumns().get(1).getSortType());
    }

}
