#Scripted User Profile JSON
Currently, a given user cannot view their own user profile via the API.  Until that functionality is implemented in the codebase, the following method can be used:

1. Setup a collection with a display template that is scripted with the following:
```
<div id="scripted-user-details">
{
	"id": "${user.getID()}",
	"username": "${user.getUsername()}",
	"firstName": "${user.getFirstName()}",
	"lastName": "${user.getLastName()}",
	"emailAddress": "${user.getEmail()}"
}
</div>
```
1. Contribute an item to that collection.  Any user that hits that item summary will have access to JSON of their user's profile.
2. Setup a shortcut URL `/s/profile` to point to the item contributed above.
