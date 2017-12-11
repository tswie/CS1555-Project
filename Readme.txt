Hello,


First, go into your sqlplus database and initiate the database using the following commands(you can use your own db for this):
@spdb.sql
@insert.sql
@admin.sql

If there are any issues running spdb.sql, drop absolutely all tables you have and then run spdb.sql, as this was an issue
for us as well. spdb.sql does drop all the tables it will soon make, but we had issues with a typo table from before.
I'm not entirely sure how the testing environment will look, but if you run this on the same db instance you ran ours on before,
search for what tables you have and drop them individually. This fixed our issue.

Then, compile the program by running javac SocialPanther.java
Then you can run SocialPanther and log in to the database again using your real pitt email and password to connect to your
mysql instance.
Then you can log in under the admin account in order to view the few friends that are set up for the account as well as
accept the two friend invites and a group member request. The admin starts out as manager of group -2, the admin group.
Using this set up will allow you to interact with every one of the functions in a premade setting.
Log in as admin using admin/admin.

Some notes to keep in mind for this:
You can refer to the insert.sql file to see the other users created and use their values in order to test adding/inviting
/messaging people.

Display friends will allow you to select one of the entries on the screen, and then will allow you to select from friends
of that friend. We intentionally did not allow for more degrees of friendhopping in this section, so you cannot see friends
of friends of friends.

Create group works as expected

Request to be added to a group works on groupID, so if you'd like, you can log in to someone elses account and request to be
added to the admin group, then log back in as admin to confirm them.

Following that, send message to user and to group work as expected. For testing purposes, remember that the admin group has
an id value of -2, in case you want to test it. The message will show up on both admin's account and the other member's account
if you added them in.

Display messages works simply
To test display new messages, you can send a message to the admin's group and then it will show up in display messages.
Alternatively, log out as admin, log in as another user, send a message to admin (id -3), then log back in as admin.
The message will be displayed in "display new messages"


Search for user is case-sensitive, but works with partial string matching so it will retrieve results.

Assuming we haven't changed the SQL file, the original user with the most messages is Leeroy Davis. If you send around 8
messages as admin, you will become the top user. Logging in to another account in insert.sql or creating a new one and making
a bunch of messages will then make that account #1, proving it works.

If you drop your own user profile (admin's id is -3), it will log you out. You can also drop the profiles in the admin's
friends list if you'd like more proof that it works. Messages require both involved user's to be dropped in order to be deleted.


ALTERNATIVELY
Instead of logging in as admin, you can create your own profiles and have them interact. Simply choose Create New User
from the main menu and you can set up interactions between them.


IMPORTANT: Before you do the next step, run @spdb.sql again and DO NOT run insert.sql, as this will conflict with the
following inserts. 

You can also type 'java SocialPanther < test.txt' and this will create a few users and interactions between them.
In this case however, the results will just be the normal UI printouts which may be a bit hard to follow.
In addition, the test.txt file only focuses on the interactions between users, such as directly sending messages/invites etc.
Functions like displayTopMessages can simply be tested by going into the UI and selecting it yourself, as these are not
user specific.

