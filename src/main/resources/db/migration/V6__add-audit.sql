    drop table if exists beer_audit;

    create table beer_audit (
        beer_style tinyint check ((beer_style between 0 and 9)),
        price decimal(38,2),
        quantity_on_hand integer,
        version integer,
        created_date datetime(6),
        created_date_audit datetime(6),
        update_date datetime(6),
        audit_id varchar(36) not null,
        id varchar(36) not null,
        beer_name varchar(50),
        audit_event_type varchar(255),
        principal_name varchar(255),
        upc varchar(255),
        primary key (audit_id)
    ) engine=InnoDB;
