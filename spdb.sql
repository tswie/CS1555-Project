DROP TABLE PROFILE CASCADE CONSTRAINTS;
DROP TABLE FRIENDS CASCADE CONSTRAINTS;
DROP TABLE PENDINGFRIENDS CASCADE CONSTRAINTS;
DROP TABLE MESSAGES CASCADE CONSTRAINTS;
DROP TABLE MESSAGERECIPIENT CASCADE CONSTRAINTS;
DROP TABLE GROUPS CASCADE CONSTRAINTS;
DROP TABLE GROUPMEMBERSHIP CASCADE CONSTRAINTS;
DROP TABLE PENDINGGROUPMEMEBERS CASCADE CONSTRAINTS;
DROP TRIGGER RECIPIENTTRIGGER;



CREATE TABLE PROFILE (
  userID varchar2(20),
  name varchar2(50) NOT NULL,
  email varchar2(50) NOT NULL,
  password varchar2(50) NOT NULL,
  date_of_birth date NOT NULL,
  lastlogin timestamp,
  CONSTRAINT PK_PROFILE PRIMARY KEY (userID)
);

/* Assumes that each friend pair is only stored once ex (John, Mary) == (Mary, John) */
CREATE TABLE FRIENDS (
  userID1 varchar2(20),
  userID2 varchar2(20),
  JDate date NOT NULL,
  message varchar2(200),
  CONSTRAINT PK_FRIENDS PRIMARY KEY (userID1, userID2) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_FRIENDS_userID1 FOREIGN KEY (userID1) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_FRIENDS_userID2 FOREIGN KEY (userID2) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE
);

/* Assumes that each person can only send one friend request and a user cannot request someone who has already requested them */
CREATE TABLE PENDINGFRIENDS (
  fromID varchar2(20),
  toID varchar2(20),
  message varchar2(200),
  CONSTRAINT PK_PENDINGFRIENDS PRIMARY KEY (fromID, toID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_PENDINGFRIENDS_fromID FOREIGN KEY (fromID) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_PENDINGFRIENDS_toID FOREIGN KEY (toID) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE
);

CREATE TABLE GROUPS (
  gID varchar2(20),
  name varchar2(50) NOT NULL,
  description varchar2(200),
  CONSTRAINT PK_GROUPS PRIMARY KEY (gID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT UN_GROUPS UNIQUE (name) INITIALLY IMMEDIATE DEFERRABLE
);

CREATE TABLE MESSAGES (
  msgID varchar2(20),
  fromID varchar2(20),
  message varchar2(200),
  toUserID varchar2(20) DEFAULT NULL,
  toGroupID varchar2(20) DEFAULT NULL,
  dateSent date NOT NULL,
  CONSTRAINT PK_MESSAGES PRIMARY KEY (msgID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_MESSAGES_fromID FOREIGN KEY (fromID) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_MESSAGES_toUserID FOREIGN KEY (toUserID) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_MESSAGES_toGroupID FOREIGN KEY (toGroupID) REFERENCES GROUPS(gID) INITIALLY IMMEDIATE DEFERRABLE
);

CREATE TABLE MESSAGERECIPIENT (
  msgID varchar2(20),
  userID varchar2(20) NOT NULL,
  CONSTRAINT PK_MESSAGERECIPIENT PRIMARY KEY (msgID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_MESSAGERECIPIENT_msgID FOREIGN KEY (msgID) REFERENCES MESSAGES(msgID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_MESSAGERECIPIENT_userID FOREIGN KEY (userID) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE
);

CREATE TABLE GROUPMEMBERSHIP (
  gID varchar2(20),
  userID varchar2(20),
  role varchar2(20) NOT NULL,
  CONSTRAINT PK_GROUPMEMBERSHIP PRIMARY KEY (gID, userID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_GROUPMEMBERSHIP_gID FOREIGN KEY (gID) REFERENCES GROUPS(gID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_GROUPMEMBERSHIP_userID FOREIGN KEY (userID) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE
);

CREATE TABLE PENDINGGROUPMEMEBERS (
  gID varchar2(20),
  userID varchar2(20),
  message varchar2(200),
  CONSTRAINT PK_PENDINGGROUPMEMBERS PRIMARY KEY (gID, userID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_PENDINGGROUPMEMEBERS_gID FOREIGN KEY (gID) REFERENCES GROUPS(gID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_PENDINGGROUPMEMEBERS_userID FOREIGN KEY (userID) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE
);

CREATE OR REPLACE TRIGGER RECIPIENTTRIGGER
  AFTER INSERT
  on MESSAGES
  DECLARE
    messageId varchar2(20);
    recipUser varchar2(20);
  BEGIN
    SELECT msgID, toUserID INTO messageId, recipUser
    FROM MESSAGES
    WHERE msgID = (
      SELECT MAX(TO_NUMBER(msgID))
      FROM MESSAGES
    );
    INSERT INTO MESSAGERECIPIENT VALUES(messageId, recipUser);
  END;
  /
