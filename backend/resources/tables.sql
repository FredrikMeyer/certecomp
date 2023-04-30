
PRAGMA foreign_keys = ON;

-- Create exercise types table
CREATE TABLE if not exists exercisetypes (id integer primary key, name unique not null);

-- Create exercise table
CREATE TABLE if not exists exercise
       (id integer primary key,
       date integer, type integer,
       foreign key(type) references exercisetypes(id));

-- Exercise sets
CREATE TABLE if not exists sets
       (id integer primary key,
       reps integer,
       goal_reps integer,
       weight float,
       exercise integer,
       foreign key(exercise) references exercise(id));
