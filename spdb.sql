DROP TABLE PROFILE CASCADE CONSTRAINTS;
DROP TABLE FRIENDS CASCADE CONSTRAINTS;
DROP TABLE PENDINGFRIENDS CASCADE CONSTRAINTS;
DROP TABLE MESSAGES CASCADE CONSTRAINTS;
DROP TABLE MESSAGERECIPIENT CASCADE CONSTRAINTS;
DROP TABLE GROUPS CASCADE CONSTRAINTS;
DROP TABLE GROUPMEMBERSHIP CASCADE CONSTRAINTS;
DROP TABLE PENDINGGROUPMEMBERS CASCADE CONSTRAINTS;
DROP VIEW GROUPMANAGERS CASCADE CONSTRAINTS;
DROP TRIGGER RECIPIENTTRIGGER;
DROP TRIGGER GROUPMEMBERSHIPLIMIT;
DROP TRIGGER ACCEPTFRIENDINVITE;
DROP TRIGGER ACCEPTGROUPINVITE;


CREATE TABLE PROFILE (
  userID number(20),
  name varchar2(50) NOT NULL,
  email varchar2(50) NOT NULL,
  password varchar2(50) NOT NULL,
  date_of_birth date NOT NULL,
  lastlogin timestamp DEFAULT NULL,
  CONSTRAINT PK_PROFILE PRIMARY KEY (userID),
  CONSTRAINT UN_PROFILE UNIQUE (email)
);

/* Assumes that each friend pair is only stored once ex (John, Mary) == (Mary, John) */
CREATE TABLE FRIENDS (
  userID1 number(20),
  userID2 number(20),
  JDate date NOT NULL,
  message varchar2(200),
  CONSTRAINT PK_FRIENDS PRIMARY KEY (userID1, userID2) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_FRIENDS_userID1 FOREIGN KEY (userID1) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_FRIENDS_userID2 FOREIGN KEY (userID2) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE
);

/* Assumes that each person can only send one friend request and a user cannot request someone who has already requested them */
CREATE TABLE PENDINGFRIENDS (
  fromID number(20),
  toID number(20),
  message varchar2(200),
  CONSTRAINT PK_PENDINGFRIENDS PRIMARY KEY (fromID, toID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_PENDINGFRIENDS_fromID FOREIGN KEY (fromID) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_PENDINGFRIENDS_toID FOREIGN KEY (toID) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE
);

CREATE TABLE GROUPS (
  gID number(20),
  name varchar2(50) NOT NULL,
  description varchar2(200),
  gLimit number(5),
  CONSTRAINT PK_GROUPS PRIMARY KEY (gID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT UN_GROUPS UNIQUE (name) INITIALLY IMMEDIATE DEFERRABLE
);

CREATE TABLE MESSAGES (
  msgID number(20),
  fromID number(20),
  message varchar2(200),
  toUserID number(20) DEFAULT NULL,
  toGroupID number(20) DEFAULT NULL,
  dateSent date NOT NULL,
  CONSTRAINT PK_MESSAGES PRIMARY KEY (msgID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_MESSAGES_fromID FOREIGN KEY (fromID) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_MESSAGES_toUserID FOREIGN KEY (toUserID) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_MESSAGES_toGroupID FOREIGN KEY (toGroupID) REFERENCES GROUPS(gID) INITIALLY IMMEDIATE DEFERRABLE
);

CREATE TABLE MESSAGERECIPIENT (
  msgID number(20),
  userID number(20) NOT NULL,
  CONSTRAINT PK_MESSAGERECIPIENT PRIMARY KEY (msgID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_MESSAGERECIPIENT_msgID FOREIGN KEY (msgID) REFERENCES MESSAGES(msgID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_MESSAGERECIPIENT_userID FOREIGN KEY (userID) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE
);

CREATE TABLE GROUPMEMBERSHIP (
  gID number(20),
  userID number(20),
  role varchar2(20) NOT NULL,
  CONSTRAINT PK_GROUPMEMBERSHIP PRIMARY KEY (gID, userID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_GROUPMEMBERSHIP_gID FOREIGN KEY (gID) REFERENCES GROUPS(gID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_GROUPMEMBERSHIP_userID FOREIGN KEY (userID) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE
);

CREATE TABLE PENDINGGROUPMEMBERS (
  gID number(20),
  userID number(20),
  message varchar2(200),
  CONSTRAINT PK_PENDINGGROUPMEMBERS PRIMARY KEY (gID, userID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_PENDINGGROUPMEMBERS_gID FOREIGN KEY (gID) REFERENCES GROUPS(gID) INITIALLY IMMEDIATE DEFERRABLE,
  CONSTRAINT FK_PENDINGGROUPMEMBERS_userID FOREIGN KEY (userID) REFERENCES PROFILE(userID) INITIALLY IMMEDIATE DEFERRABLE
);


--Logs the message in MESSAGERECIPIENT
CREATE OR REPLACE TRIGGER RECIPIENTTRIGGER
  AFTER INSERT
  on MESSAGES
  REFERENCING NEW as NEW
  FOR EACH ROW
  DECLARE
    messageId number(20);
    recipUser number(20);
  BEGIN
    INSERT INTO MESSAGERECIPIENT VALUES(:NEW.msgID, :NEW.toUserID);
  END;
/

show errors;

--Rejects group member if it would go over the group's limit
CREATE OR REPLACE TRIGGER GROUPMEMBERSHIPLIMIT
  BEFORE INSERT OR UPDATE
  on GROUPMEMBERSHIP
  REFERENCING NEW AS newMember
  FOR EACH ROW
  DECLARE
    groupMembers INTEGER;
    groupLimit INTEGER;
    memberCount INTEGER;
    custom_error EXCEPTION;
    PRAGMA EXCEPTION_INIT ( custom_error, -20003);

  BEGIN
    SELECT COUNT(*) AS GROUPCOUNTS INTO groupMembers
    FROM GROUPMEMBERSHIP
    WHERE GROUPMEMBERSHIP.gID = :newMember.gID;

    SELECT glimit INTO groupLimit
    FROM GROUPS
    WHERE gID = :newMember.gID;

    IF groupMembers >= groupLimit
    THEN
      RAISE_APPLICATION_ERROR(-20003, 'This group is full');
    END IF;
  END;
/

show errors;

--Deletes old requests for friends/groups
CREATE OR REPLACE TRIGGER ACCEPTFRIENDINVITE
  AFTER INSERT
  ON FRIENDS
  REFERENCING new AS new old AS old
  FOR EACH ROW
  BEGIN
    DELETE FROM PENDINGFRIENDS WHERE :new.userID1 = fromID AND :new.userID2 = toID;
    DELETE FROM PENDINGFRIENDS WHERE :new.userID2 = fromID AND :new.userID1 = toID;
  END;
/

show errors;

CREATE OR REPLACE TRIGGER ACCEPTGROUPINVITE
  AFTER INSERT
  ON GROUPMEMBERSHIP
  REFERENCING new AS new old AS old
  FOR EACH ROW
  BEGIN
      DELETE FROM PENDINGGROUPMEMBERS WHERE PENDINGGROUPMEMBERS.gID = :new.gID AND PENDINGGROUPMEMBERS.userID = :new.userID;
  END;
/
show errors;

CREATE OR REPLACE TRIGGER DROPUSER
  AFTER DELETE
  ON PROFILE
  REFERENCING new AS new old AS old
  FOR EACH ROW
  BEGIN
    DELETE FROM FRIENDS WHERE USERID1 = :old.userID OR USERID2 = :old.userID;
    DELETE FROM PENDINGFRIENDS WHERE TOID = :old.userID or FROMID = :old.userID;
    DELETE FROM MESSAGERECIPIENT WHERE MESSAGERECIPIENT.userID = :old.userID;
    DELETE FROM GROUPMEMBERSHIP WHERE GROUPMEMBERSHIP.USERID = :old.userID;
    DELETE FROM PENDINGGROUPMEMBERS WHERE PENDINGGROUPMEMBERS.userID = :old.userID;
    UPDATE MESSAGES SET toUserID = NULL WHERE Messages.toUserID = :old.userID;
    UPDATE MESSAGES SET fromID = NULL WHERE Messages.fromID = :old.userID;
    DELETE FROM MESSAGES WHERE Messages.toUserID IS NULL AND Messages.fromID IS NULL;
  END;
/
  SHOW ERRORS;