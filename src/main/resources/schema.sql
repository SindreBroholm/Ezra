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
    Name           varchar(100) not null,
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

create table ezra.family_data
(
    family_id       varchar(50) unique not null,
    user_one     int                not null,
    user_two     int                not null,
    pending_request boolean            not null,
    are_family      boolean            not null,
    primary key (family_id),
    foreign key (user_one) references user_data (Id) ON DELETE CASCADE,
    foreign key (user_two) references user_data (Id) ON DELETE CASCADE
);

create table ezra.family_request
(
    id           int auto_increment unique,
    User_Id      int          not null,
    member_Email varchar(360) not null,
    have_Joined  boolean,
    primary key (id),
    foreign key (User_id) references user_data (id) ON DELETE CASCADE
);
drop table ezra.family_request;


drop table ezra.family_data;