ALTER TABLE beer_order_line
    ADD COLUMN status tinyint check ((status between 0 and 1));