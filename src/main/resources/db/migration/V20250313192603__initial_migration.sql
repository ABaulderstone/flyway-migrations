
    create table users (
        id bigint not null auto_increment,
        createdAt datetime(6),
        updatedAt datetime(6),
        email varchar(255),
        location varchar(255),
        password varchar(255),
        role enum ('ADMIN','CANDIDATE','COACH','STUDENT','SUPPORT'),
        primary key (id)
    ) engine=InnoDB;

    alter table users 
       add constraint UK6dotkott2kjsp8vw4d0m25fb7 unique (email);
