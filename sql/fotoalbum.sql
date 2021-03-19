CREATE TABLE fotoalbum_users (
  fullname          NVARCHAR2(50)                       NOT NULL,
  username          NVARCHAR2(50)                       NOT NULL,
  email             NVARCHAR2(50)                       NOT NULL,
  userpass          NUMBER                              NOT NULL,
  address           NVARCHAR2(100)                      NOT NULL,
  age               NUMBER                              NOT NULL,
  registration_time TIMESTAMP DEFAULT current_timestamp NOT NULL,
  user_level        NUMBER(1)                           NOT NULL,
  CONSTRAINT fupk PRIMARY KEY (username)
);

CREATE TABLE fotoalbum_images (
  uploadtime TIMESTAMP       NOT NULL,
  filename   NVARCHAR2(1024) NOT NULL,
  category   NVARCHAR2(50)   NOT NULL,
  location   NVARCHAR2(1024) NOT NULL,
  uploader   NVARCHAR2(50)   NOT NULL,
  imagedata  blob,
  CONSTRAINT fipk PRIMARY KEY (uploadtime)
);

CREATE TABLE fotoalbum_uservotes (
  username   NVARCHAR2(50)                       NOT NULL,
  uploadtime TIMESTAMP                           NOT NULL,
  score      NUMBER(1)                           NOT NULL,
  votetime   TIMESTAMP DEFAULT current_timestamp NOT NULL,
  CONSTRAINT fauv PRIMARY KEY (username, uploadtime)
);

CREATE TABLE fotoalbum_imgstats (
  uploadtime    TIMESTAMP NOT NULL,
  image_width   NUMBER    NOT NULL,
  image_height  NUMBER    NOT NULL,
  score         NUMBER    NOT NULL,
  avgscore      FLOAT     NOT NULL,
  score1_votes  NUMBER    NOT NULL,
  score2_votes  NUMBER    NOT NULL,
  score3_votes  NUMBER    NOT NULL,
  score4_votes  NUMBER    NOT NULL,
  score5_votes  NUMBER    NOT NULL,
  seen_times    NUMBER    NOT NULL,
  lastvotescore NUMBER    NOT NULL,
  CONSTRAINT fisk PRIMARY KEY (uploadtime)
);


CREATE TABLE fotoalbum_userstats (
  username          NVARCHAR2(50) NOT NULL,
  lastlogintime     TIMESTAMP     NOT NULL,
  lastvotetime      TIMESTAMP     NOT NULL,
  lastvotescore     NUMBER        NOT NULL,
  score1_votes      NUMBER        NOT NULL,
  score2_votes      NUMBER        NOT NULL,
  score3_votes      NUMBER        NOT NULL,
  score4_votes      NUMBER        NOT NULL,
  score5_votes      NUMBER        NOT NULL,
  number_of_logins  NUMBER        NOT NULL,
  imguploadnumber   NUMBER        NOT NULL,
  imgdelnumber      NUMBER        NOT NULL,
  userdelnumber     NUMBER        NOT NULL,
  adminpromonumber  NUMBER        NOT NULL,
  admindemotenumber NUMBER        NOT NULL,
  CONSTRAINT fustk PRIMARY KEY (username)
);


CREATE TABLE fotoalbum_useractions (
  username   NVARCHAR2(50)                       NOT NULL,
  logintime  TIMESTAMP                           NOT NULL,
  action     NVARCHAR2(200)                      NOT NULL,
  actiontime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fuapk PRIMARY KEY (actiontime)
);


CREATE TABLE fotoalbum_categories (
  creationtime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  username     NVARCHAR2(50)                       NOT NULL,
  category     NVARCHAR2(200)                      NOT NULL,
  CONSTRAINT fc_pk PRIMARY KEY (category)
);


CREATE TABLE fotoalbum_locations (
  creationtime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  username     NVARCHAR2(50)                       NOT NULL,
  location     NVARCHAR2(1024)                     NOT NULL,
  CONSTRAINT fl_pk PRIMARY KEY (location)
);

select * from fotoalbum_users;
select * from fotoalbum_userstats;
select * from fotoalbum_categories;
select * from fotoalbum_locations;
select * from fotoalbum_images;
select * from fotoalbum_uservotes;

SELECT * FROM fotoalbum_imgstats ORDER BY uploadtime ASC;
SELECT * FROM fotoalbum_imgstats ORDER BY uploadtime DESC;
SELECT * FROM fotoalbum_imgstats ORDER BY avgscore ASC;
SELECT * FROM fotoalbum_imgstats ORDER BY avgscore DESC;
SELECT * FROM fotoalbum_imgstats ORDER BY seen_times ASC;
SELECT * FROM fotoalbum_imgstats ORDER BY seen_times DESC;
