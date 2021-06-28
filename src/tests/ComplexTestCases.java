package tests;

public class ComplexTestCases {

    public final static String Query1 = "SELECT author.name, count(book.id), sum(book.cost) \n" +
            "FROM author \n" +
            "LEFT JOIN book ON (author.id = book.author_id) \n" +
            "GROUP BY author.name \n" +
            "HAVING COUNT(*) > 1 AND SUM(book.cost) > 500\n" +
            "LIMIT 10;";

    public final static String Query2 = "select * from (select * from (select * from table_name_2))";

    public final static String Query3 = "select aaa, bb, ccccccc from my_table order by a";

    public final static String Query4 = "SELECT SalesOrderID,\n" +
            "LineTotal,\n" +
            "(SELECT AVG(LineTotal)\n" +
            "   FROM Sales.SalesOrderDetail) AS AverageLineTotal\n" +
            "FROM   Sales.SalesOrderDetail";

    public final static String Query5 = "select aaa, bb, (             select avg( x          ) from t1     ) from my_table";

    public final static String Query6 = "select avg(x), from t1";

}
