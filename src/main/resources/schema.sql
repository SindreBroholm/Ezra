create schema ezra;

create table ezra.user_data
(
    Id           int auto_increment unique,
    Email        varchar(320) not null unique,
    Firstname    varchar(100) not null,
    Lastname     varchar(100) not null,
    Password     varchar(300) not null,
    Phone_Number varchar(15),
    primary key (Id)
);


create table ezra.board_data
(
    Id             int unique auto_increment,
    Name           varchar(100) not null unique,
    Contact_Name   varchar(100) not null,
    Contact_Number varchar(15),
    Contact_Email  varchar(320) not null,
    Homepage       varchar(100),
    primary key (Id)
);

create table ezra.membership
(
    Type varchar(56) not null,
    primary key (Type)
);
insert into ezra.membership(Type)
VALUES ('MASTER'),
       ('ADMIN'),
       ('MEMBER'),
       ('FOLLOWER'),
       ('VISITOR');

create table ezra.user_role
(
    User_Id         int          not null,
    Board_Id        int          not null,
    Membership_Type varchar(100) not null,
    Pending_Member  boolean,
    primary key (User_Id, Board_Id),
    FOREIGN KEY (User_Id) references user_data (Id) ON DELETE CASCADE,
    foreign key (Board_Id) references board_data (Id) ON DELETE CASCADE,
    foreign key (membership_Type) references membership (Type) ON DELETE CASCADE
);


create table ezra.event_data
(
    Id               int unique auto_increment,
    Board_Id         int           not null,
    Message          varchar(5000) not null,
    Datetime_From    datetime      not null,
    Datetime_To      datetime      not null,
    Datetime_Created datetime      not null,
    Membership_Type  varchar(100)  not null,
    Location         varchar(150),
    Event_Name       varchar(150)  not null,
    foreign key (Membership_Type) references membership (Type) ON DELETE CASCADE,
    foreign key (Board_Id) references board_data (Id) ON DELETE CASCADE
);

create table ezra.notification
(
    Id               int auto_increment unique not null,
    Board_Id         int                       not null,
    Message          varchar(5000)             not null,
    Datetime_Created datetime                  not null,
    Membership_Type  varchar(100)              not null,
    primary key (Id),
    foreign key (Board_Id) references board_data (Id) ON DELETE CASCADE,
    foreign key (Membership_Type) references membership (Type) ON DELETE CASCADE
);
