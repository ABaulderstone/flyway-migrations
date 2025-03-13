
    create table users (
        id bigint not null auto_increment,
        createdAt datetime(6),
        updatedAt datetime(6),
        email varchar(255),
        password varchar(255),
        primary key (id)
    ) engine=InnoDB;
