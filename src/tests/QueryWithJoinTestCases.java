package tests;

import com.newjob.parser.QueryParser;
import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.TermType;
import com.newjob.parser.exceptions.InvalidQueryFormatException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QueryWithJoinTestCases {

    @Test
    public void query01() throws InvalidQueryFormatException {
        // Simple joins

        // Arrange
        final String query = "select * from Boards\n" +
                "  left join Lists ON Boards.Id = Lists.BoardId\n" +
                "  right join Users ON Lists.LastModifiedById = Users.Id\n" +
                "  left join Dogs ON Boards.Id = Lists.BoardId\n" +
                "  right join Cats ON Lists.LastModifiedById = Users.Id\n" +
                "  left join Humans ON Boards.Id = Lists.BoardId\n" +
                "  right join Animals ON Lists.LastModifiedById = Users.Id\n" +
                "  full join Cards ON Users.Id = Cards.LastModifiedById";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert

    }

    @Test
    public void query02() throws InvalidQueryFormatException {
        // Join with subquery's result set

        // Arrange
        final String query = "select * from Boards\n" +
                "  left join Lists ON Boards.Id = Lists.BoardId\n" +
                "  right join Users ON Lists.LastModifiedById = Users.Id\n" +
                "  left join Dogs ON Boards.Id = Lists.BoardId\n" +
                "  right join Cats ON Lists.LastModifiedById = Users.Id\n" +
                "  full join Men ON Lists.LastModifiedById = Users.Id\n" +
                "  join Pets as PPP ON PPP.LastModifiedById = Users.Id\n" +
                "  left join Humans ON Boards.Id = Lists.BoardId\n" +
                "  join NotPets as AAA ON PPP.LastModifiedById = Users.Id\n" +
                "  right join Animals ON Lists.LastModifiedById = Users.Id\n" +
                "  inner join (SELECT meta_value As Prenom, post_id FROM wp_postmeta join Tab5 on t1 = t2) AS a ON wp_woocommerce_order_items.order_id = a.post_id" +
                "  where Users.email = 'mail@mail.com' limit 10";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert

    }

    @Test
    public void query03() throws InvalidQueryFormatException {
        // Join with subquery's result set

        // Arrange
        final String query = "select * from Boards\n" +
                "  left join Lists ON Boards.Id = Lists.BoardId\n" +
                "  right join Users ON Lists.LastModifiedById = Users.Id\n" +
                "  left join Dogs ON Boards.Id = Lists.BoardId\n" +
                "  right join Cats ON Lists.LastModifiedById = Users.Id\n" +
                "  full join Men ON Lists.LastModifiedById = Users.Id\n" +
                "  join Pets as PPP ON PPP.LastModifiedById = Users.Id\n" +
                "  left join Humans ON Boards.Id = Lists.BoardId\n" +
                "  join NotPets as AAA ON PPP.LastModifiedById = Users.Id\n" +
                "  inner join (SELECT meta_value As Prenom, post_id FROM wp_postmeta) AS a ON wp_woocommerce_order_items.order_id = a.post_id" +
                "  right join Animals ON Lists.LastModifiedById = Users.Id\n" +
                "  limit 10";

        // Act
        Query res = QueryParser.parseQuery(query);

        // Assert

    }

}
