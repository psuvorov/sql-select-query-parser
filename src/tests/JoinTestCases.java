package tests;

import com.newjob.parser.QueryParser;
import com.newjob.parser.domain.Query;
import com.newjob.parser.domain.enums.JoinType;
import com.newjob.parser.domain.enums.TermType;
import com.newjob.parser.exceptions.InvalidQueryFormatException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JoinTestCases {

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
        assertEquals(1, res.getColumns().size());
        assertEquals(1, res.getFromSources().size());
        assertEquals(7, res.getJoins().size());

        assertEquals(JoinType.Left, res.getJoins().get(0).getJoinType());
        assertEquals("Lists", res.getJoins().get(0).getReferencedTableName());
        assertEquals("Boards.Id = Lists.BoardId", res.getJoins().get(0).getJoinClause());

        assertEquals(JoinType.Right, res.getJoins().get(1).getJoinType());
        assertEquals("Users", res.getJoins().get(1).getReferencedTableName());
        assertEquals("Lists.LastModifiedById = Users.Id", res.getJoins().get(1).getJoinClause());

        assertEquals(JoinType.Full, res.getJoins().get(6).getJoinType());
        assertEquals("Cards", res.getJoins().get(6).getReferencedTableName());
        assertEquals("Users.Id = Cards.LastModifiedById", res.getJoins().get(6).getJoinClause());
    }

    @Test
    public void query02() throws InvalidQueryFormatException {
        // Join with subquery's result set

        // Arrange
        final String query = "select * from Boards\n" +
                "  left join Lists ON Boards.Id = Lists.BoardId\n" +
                "  left join Lists ON Boards.Id = Lists.BoardId\n" +
                "  right join Users as Uu1 ON Lists.LastModifiedById = Users.Id\n" +
                "  right join Users as Uu2 ON Lists.LastModifiedById = Users.Id\n" +
                "  left join Dogs dd ON Boards.Id = Lists.BoardId\n" +
                "  right join Cats ON (Lists.LastModifiedById = Users.Id)\n" +
                "  full join Men ON Lists.LastModifiedById = Users.Id\n" +
                "  join Pets as PPP1 ON PPP.LastModifiedById = Users.Id\n" +
                "  join Pets PPP2 ON PPP.LastModifiedById = Users.Id\n" +
                "  left join Humans ON Boards.Id = Lists.BoardId\n" +
                "  join NotPets as AAA ON PPP.LastModifiedById = Users.Id\n" +
                "  right join Animals ON Lists.LastModifiedById = Users.Id\n" +
                "  inner join (SELECT meta_value As Prenom, post_id FROM wp_postmeta join Tab5 on t1 = t2) AS a1 ON wp_woocommerce_order_items.order_id = a.post_id" +
                "  full join (SELECT meta_value As Prenom, post_id FROM wp_postmeta join Tab6 on t1 = t2) a2 ON wp_woocommerce_order_items.order_id = a.post_id" +
                "  where Users.email = 'mail@mail.com' limit 10";

        // Act
        Query res = QueryParser.parseQuery(query);
        Query referencedSubquery1 = res.getJoins().get(12).getReferencedSubquery();
        Query referencedSubquery2 = res.getJoins().get(13).getReferencedSubquery();

        // Assert
        assertEquals(1, res.getColumns().size());
        assertEquals(1, res.getFromSources().size());
        assertEquals(14, res.getJoins().size());

        assertEquals(JoinType.Inner, res.getJoins().get(12).getJoinType());
        assertEquals("", res.getJoins().get(12).getReferencedTableName());
        assertEquals("a1", res.getJoins().get(12).getReferencedAlias());
        assertEquals("wp_woocommerce_order_items.order_id = a.post_id", res.getJoins().get(12).getJoinClause());

        assertEquals(2, referencedSubquery1.getColumns().size());
        assertEquals(JoinType.Inner, referencedSubquery1.getJoins().get(0).getJoinType());
        assertEquals("Tab5", referencedSubquery1.getJoins().get(0).getReferencedTableName());
        assertEquals("t1 = t2", referencedSubquery1.getJoins().get(0).getJoinClause());

        assertEquals(JoinType.Full, res.getJoins().get(13).getJoinType());
        assertEquals("", res.getJoins().get(13).getReferencedTableName());
        assertEquals("a2", res.getJoins().get(13).getReferencedAlias());
        assertEquals("wp_woocommerce_order_items.order_id = a.post_id", res.getJoins().get(13).getJoinClause());

        assertEquals(2, referencedSubquery2.getColumns().size());
        assertEquals(JoinType.Inner, referencedSubquery2.getJoins().get(0).getJoinType());
        assertEquals("Tab6", referencedSubquery2.getJoins().get(0).getReferencedTableName());
        assertEquals("t1 = t2", referencedSubquery2.getJoins().get(0).getJoinClause());
    }

}
