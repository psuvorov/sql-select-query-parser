# SQL SELECT query parser

Creates overall SELECT query info like:

```sql 
select aa, bb, (select avg(x) from table_with_x) from my_table
```

```_____________________________________
OVERALL QUERY INFO:
	COLUMNS:
		aa
		bb
		_____________________________________
		OVERALL QUERY INFO:
			COLUMNS:
				avg(x)
			SOURCES:
				Table Name: table_with_x
		-------------------------------------
	SOURCES:
		Table Name: my_table
-------------------------------------
```


```sql
  select * from Boards
  left join Lists LISTS ON Boards.Id = Lists.BoardId
  right join Users ON Lists.LastModifiedById = Users.Id
  left join Dogs DOGS ON Boards.Id = Lists.BoardId
  right join Cats ON Lists.LastModifiedById = Users.Id
  left join Humans ON Boards.Id = Lists.BoardId
  right join Animals ON Lists.LastModifiedById = Users.Id
  full join Cards ON Users.Id = Cards.LastModifiedById
  inner join (SELECT meta_value As Prenom, post_id FROM wp_postmeta join Tab5 on t1 = t2) AS a1 
                  ON wp_woocommerce_order_items.order_id = a.post_id
```

```_____________________________________
OVERALL QUERY INFO:
	COLUMNS:
		*
	SOURCES:
		Table Name: Boards
	JOINS:
		Join Type: Left
		Referenced Table: Lists AS LISTS

		Join Type: Right
		Referenced Table: Users

		Join Type: Left
		Referenced Table: Dogs AS DOGS

		Join Type: Right
		Referenced Table: Cats

		Join Type: Left
		Referenced Table: Humans

		Join Type: Right
		Referenced Table: Animals

		Join Type: Full
		Referenced Table: Cards

		Join Type: Inner
		SubQuery AS a1:
		_____________________________________
		OVERALL QUERY INFO:
			COLUMNS:
				meta_value AS Prenom
				 AS post_id
			SOURCES:
				Table Name: wp_postmeta
			JOINS:
				Join Type: Inner
				Referenced Table: Tab5

		-------------------------------------
-------------------------------------
```


```sql
  SELECT Products.*
	FROM Products, NotProducts, NotProductsAtAll
	     INNER JOIN 
	(
	    SELECT ProductID, Sum(Quantity) as QuantitySum
	    from
	    (
	        SELECT ProductID, Quantity  
	        FROM BasketItems  
	    ) v
	    GROUP BY ProductID
	) ProductTotals
	    ON Products.ID = ProductTotals.ProductID
	ORDER BY QuantitySum DESC
```

```
_____________________________________
OVERALL QUERY INFO:
	COLUMNS:
		Products.*
	SOURCES:
		Table Name: Products
		Table Name: NotProducts
		Table Name: NotProductsAtAll
	JOINS:
		Join Type: Inner
		SubQuery AS ProductTotals:
		_____________________________________
		OVERALL QUERY INFO:
			COLUMNS:
				ProductID
				Sum(Quantity) AS QuantitySum
			SOURCES:
				SubQuery AS v:
				_____________________________________
				OVERALL QUERY INFO:
					COLUMNS:
						ProductID
						 AS Quantity
					SOURCES:
						Table Name: BasketItems
				-------------------------------------
			GROUP BY:
				ProductID
		-------------------------------------
	ORDER BY:
		QuantitySum Desc
-------------------------------------
```
