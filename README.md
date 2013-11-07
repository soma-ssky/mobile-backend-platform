#REST API#

| address | method | function |
|---|:---:|---|
| /(version)/classes/(className) | POST | Creating Objects  | 
| /(version)/classes/(className)/(objectId) | GET | Retrieving(Fetching) Objects
| /(version)/classes/(className)/(objectId) | PUT | Updating Objects
| /(version)/classes/(className) | GET | Querying(Getting) Object
| /(version)/classes/(className)/(objectId) | DELETE | Deleting Objects
| /(version)/batch | POST | Batch Operation

currently version is 1


##Object##

##Creating Objects##

To create a new object on Parse, send a POST request to the class URL containing the contents of the object. For example, to create the object described above:

    curl -X POST \
    -H "Content-Type: application/json" \
    -d '{"score":1337,"playerName":"Sean Plott","cheatMode":false}' \
    <server_address>/1/classes/GameScore

When the creation is successful, the HTTP response is a 201 Created and the Location header contains the object URL for the new object:

    Status: 201 Created
    Location: <server_address>/1/classes/GameScore/Ed1nuqPvcm
    The response body is a JSON object containing the objectId and the createdAt timestamp of the newly-created object:

    {
        "createdAt": "2011-08-20T02:06:57.931Z",
        "objectId": "e34f189e-f910-4a28-9eb7-3bc85a383eee"
    }


##Retrieving Objects##

Once you've created an object, you can retrieve its contents by sending a GET request to the object URL returned in the location header. For example, to retrieve the object we created above:

    curl -X GET \
    -H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
    -H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
    <server_address>/1/classes/GameScore/Ed1nuqPvcm


The response body is a JSON object containing all the user-provided fields, plus the createdAt, updatedAt, and objectId fields:

    {
        "score": 1337,
        "playerName": "Sean Plott",
        "cheatMode": false,
        "skills": [
            "pwnage",
            "flying"
        ],
        "createdAt": "2011-08-20T02:06:57.931Z",
        "updatedAt": "2011-08-20T02:06:57.931Z",
        "objectId": "Ed1nuqPvcm"
    }
    
When retrieving objects that have pointers to children, you can fetch child objects by using the include option. For instance, to fetch the object pointed to by the "game" key:

    curl -X GET \
        -H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
        -H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
        -G \
        --data-urlencode 'include=game' \
        <server_address>/1/classes/GameScore/Ed1nuqPvcm


##Updating Objects##

To change the data on an object that already exists, send a PUT request to the object URL. Any keys you don't specify will remain unchanged, so you can update just a subset of the object's data. For example, if we wanted to change the score field of our object:

    curl -X PUT \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-H "Content-Type: application/json" \
  	-d '{"score":73453}' \
  	<server_address>/1/classes/GameScore/Ed1nuqPvcm

The response body is a JSON object containing just an updatedAt field with the timestamp of the update.

	{
  		"updatedAt": "2011-08-21T18:02:52.248Z"
	}


##Counters##

To help with storing counter-type data, Parse provides the ability to atomically increment (or decrement) any number field. So, we can increment the score field like so:

	curl -X PUT \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-H "Content-Type: application/json" \
  	-d '{"score":{"__op":"Increment","amount":1}}' \
  	<server_address>/1/classes/GameScore/Ed1nuqPvcm


##Arrays##

To help with storing array data, there are three operations that can be used to atomically change an array field:

Add appends the given array of objects to the end of an array field.
AddUnique adds only the given objects which aren't already contained in an array field to that field. The position of the insert is not guaranteed.
Remove removes all instances of each given object from an array field.
Each method takes an array of objects to add or remove in the "objects" key. For example, we can add items to the set-like "skills" field like so:

	curl -X PUT \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-H "Content-Type: application/json" \
  	-d '{"skills":{"__op":"AddUnique","objects":["flying","kungfu"]}}' \
  	<server_address>/1/classes/GameScore/Ed1nuqPvcm


##Relations##

In order to update Relation types, Parse provides special operators to atomically add and remove objects to a relation. So, we can add an object to a relation like so:

	curl -X PUT \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-H "Content-Type: application/json" \
  	-d '{"opponents":{"__op":"AddRelation","objects":[{"__type":"Pointer","className":"Player","objectId":"Vx4nudeWn"}]}}' \
  	<server_address>/1/classes/GameScore/Ed1nuqPvcm


To remove an object from a relation, you can do:

	curl -X PUT \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-H "Content-Type: application/json" \
  	-d '{"opponents":{"__op":"RemoveRelation","objects":[{"__type":"Pointer","className":"Player","objectId":"Vx4nudeWn"}]}}' \
  	<server_address>/1/classes/GameScore/Ed1nuqPvcm


##Deleting Objects##

To delete an object from the Parse Cloud, send a DELETE request to its object URL. For example:

	curl -X DELETE \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	<server_address>/1/classes/GameScore/Ed1nuqPvcm

You can delete a single field from an object by using the Delete operation:

	curl -X PUT \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-H "Content-Type: application/json" \
  	-d '{"opponents":{"__op":"Delete"}}' \
  	<server_address>/1/classes/GameScore/Ed1nuqPvcm


##Batch Operations##

To reduce the amount of time spent on network round trips, you can create, update, or delete several objects in one call, using the batch endpoint.
Each command in a batch has method, path, and body parameters that specify the HTTP command that would normally be used for that command. The commands are run in the order they are given. For example, to create a couple of GameScore objects:

	curl -X POST \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-H "Content-Type: application/json" \
  	-d '{
		"requests": [
          	{
            	"method": "POST",
            	"path": "/1/classes/GameScore",
            	"body": {
             		"score": 1337,
              		"playerName": "Sean Plott"
            	}
			}, {	
            	"method": "POST",
            	"path": "/1/classes/GameScore",
            	"body": {
              		"score": 1338,
              		"playerName": "ZeroCool"
            	}
          	}
    	]
	}' \
  	<server_address>/1/batch
  	
The response from batch will be a list with the same number of elements as the input list. Each item in the list with be a dictionary with either the success or error field set. The value of success will be the normal response to the equivalent REST command:

	{
  		"success": {
    		"createdAt": "2012-06-15T16:59:11.276Z",
    		"objectId": "YAfSAWwXbL"
  		}
	}

The value of error will be an object with a numeric code and error string:

	{
  		"error": {
    		"code": 101,
    		"error": "object not found for delete"
  		}
	}

Other commands that work in a batch are update and delete.

	curl -X POST \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-H "Content-Type: application/json" \
  	-d '{
		"requests": [
          	{
            	"method": "PUT",
            	"path": "/1/classes/GameScore/Ed1nuqPvcm",
            	"body": {
              		"score": 999999
            	}
          	}, {
            	"method": "DELETE",
            	"path": "/1/classes/GameScore/Cpl9lrueY5"
          	}
		]
	}' \
	<server_address>/1/batch

#Queries#

##Basic Queries##

You can retrieve multiple objects at once by sending a GET request to the class URL. Without any URL parameters, this simply lists objects in the class:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	<server_address>/1/classes/GameScore


The return value is a JSON object that contains a results field with a JSON array that lists the objects.

	{
  		"results": [{
			"playerName": "Jang Min Chul",
      		"updatedAt": "2011-08-19T02:24:17.787Z",
      		"cheatMode": false,
      		"createdAt": "2011-08-19T02:24:17.787Z",
      		"objectId": "A22v5zRAgd",
      		"score": 80075
    	}, {
      		"playerName": "Sean Plott",
      		"updatedAt": "2011-08-21T18:02:52.248Z",
      		"cheatMode": false,
      		"createdAt": "2011-08-20T02:06:57.931Z",
      		"objectId": "Ed1nuqPvcm",
      		"score": 73453
    	}]
	}


##Query Constraints##

There are several ways to put constraints on the objects found, using the where URL parameter. The value of the where parameter should be encoded JSON. Thus, if you look at the actual URL requested, it would be JSON-encoded, then URL-encoded. The simplest use of the where parameter is constraining the value for keys. For example, if we wanted to retrieve Sean Plott's scores that were not in cheat mode, we could do:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'where={"playerName":"Sean Plott","cheatMode":false}' \
	<server_address>/1/classes/GameScore

The values of the where parameter also support comparisons besides exact matching. Instead of an exact value, provide a hash with keys corresponding to the comparisons to do. The where parameter supports these options:


| method | function |
|---|---|
| $lt | Less Than | 
| $lte | Less Than Or Equal To | 
| $gt | Greater Than | 
| $gte | Greater Than Or Equal To | 
| $ne | Not Equal To | 
| $in | Contained In | 
| $nin | Not Contained in | 
| $exists | A value is set for the key | 

For example, to retrieve scores between 1000 and 3000, including the endpoints, we could issue:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'where={"score":{"$gte":1000,"$lte":3000}}' \
  	<server_address>/1/classes/GameScore

To retrieve scores equal to an odd number below 10, we could issue:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'where={"score":{"$in":[1,3,5,7,9]}}' \
  	<server_address>/1/classes/GameScore

To retrieve scores not by a given list of players we could issue:

	curl -X GET \
	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'where={"playerName":{"$nin":["Jonathan Walsh","Dario Wunsch","Shawn Simon"]}}' \
  	<server_address>/1/classes/GameScore

To retrieve documents with the score set, we could issue:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'where={"score":{"$exists":true}}' \
  	<server_address>/1/classes/GameScore

To retrieve documents without the score set, we could issue:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'where={"score":{"$exists":false}}' \
  	<server_address>/1/classes/GameScore

If you have a class containing sports teams and you store a user's hometown in the user class, you can issue one query to find the list of users whose hometown teams have winning records. The query would look like:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'where={"hometown":{"$select":{"query":{"className":"Team","where":{"winPct":{"$gt":0.5}}},"key":"city"}}}' \
  	<server_address>/1/classes/_User

You can use the order parameter to specify a field to sort by. Prefixing with a negative sign reverses the order. Thus, to retrieve scores in ascending order:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'order=score' \
  	<server_address>/1/classes/GameScore

And to retrieve scores in descending order:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'order=-score' \
  	<server_address>/1/classes/GameScore

You can sort by multiple fields by passing order a comma-separated list. To retrieve documents that are ordered by scores in ascending order and the names in descending order:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'order=score,-name' \
  	<server_address>/1/classes/GameScore

You can use the limit and skip parameters for pagination. limit defaults to 100, but anything from 1 to 1000 is a valid limit. Thus, to retrieve 200 objects after skipping the first 400:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'limit=200' \
  	--data-urlencode 'skip=400' \
  	<server_address>/1/classes/GameScore

You can restrict the fields returned by passing keys a comma-separated list. To retrieve documents that contain only the score and playerName fields (and also special built-in fields such as objectId, createdAt, and updatedAt):

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'keys=score,playerName' \
  	<server_address>/1/classes/GameScore

All of these parameters can be used in combination with each other.

##Queries on Array Values##

For keys with an array type, you can find objects where the key's array value contains 2 by:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'where={"arrayKey":2}' \
  	<server_address>/1/classes/RandomObject

You can also use the $all operator to find objects with an array field which contains each of the values 2, 3, and 4 by:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'where={"arrayKey":{"$all":[2,3,4]}}' \
  	<server_address>/1/classes/RandomObject

##Relational Queries##

There are several ways to issue queries for relational data. If you want to retrieve objects where a field matches a particular object, you can use a where clause with a Pointer encoded with __type just like you would use other data types. For example, if each Comment has a Post object in its post field, you can fetch comments for a particular Post:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'where={"post":{"__type":"Pointer","className":"Post","objectId":"8TOXdXf3tz"}}' \
  	<server_address>/1/classes/Comment

If you want to retrieve objects where a field contains an object that matches another query, you can use the $inQuery operator. Note that the default limit of 100 and maximum limit of 1000 apply to the inner query as well, so with large data sets you may need to construct queries carefully to get the desired behavior. For example, imagine you have Post class and a Comment class, where each Comment has a relation to its parent Post. You can find comments on posts with images by doing:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'where={"post":{"$inQuery":{"where":{"image":{"$exists":true}},"className":"Post"}}}' \
  	<server_address>/1/classes/Comment

If you want to retrieve objects where a field contains an object that does not match another query, you can use the $notInQuery operator. Imagine you have Post class and a Comment class, where each Comment has a relation to its parent Post. You can find comments on posts without images by doing:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'where={"post":{"$notInQuery":{"where":{"image":{"$exists":true}},"className":"Post"}}}' \
  	<server_address>/1/classes/Comment

If you want to retrieve objects that are members of Relation field of a parent object, you can use the $relatedTo operator. Imagine you have a Post class and User class, where each Post can be liked by many users. If the Users that liked a Post was stored in a Relation on the post under the key likes, you, can the find the users that liked a particular post by:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'where={"$relatedTo":{"object":{"__type":"Pointer","className":"Post","objectId":"8TOXdXf3tz"},"key":"likes"}}' \
  	<server_address>/1/users

In some situations, you want to return multiple types of related objects in one query. You can do this by passing the field to include in the include parameter. For example, let's say you are retrieving the last ten comments, and you want to retrieve their related posts at the same time:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'order=-createdAt' \
  	--data-urlencode 'limit=10' \
  	--data-urlencode 'include=post' \
  	<server_address>/1/classes/Comment

Instead of being represented as a Pointer, the post field is now expanded into the whole object. __type is set to Object and className is provided as well. For example, a Pointer to a Post could be represented as:

	{
  		"__type": "Pointer",
  		"className": "Post",
  		"objectId": "8TOXdXf3tz"
	}
	
When the query is issued with an include parameter for the key holding this pointer, the pointer will be expanded to:

	{
  		"__type": "Object",
  		"className": "Post",
  		"objectId": "8TOXdXf3tz",
  		"createdAt": "2011-12-06T20:59:34.428Z",
  		"updatedAt": "2011-12-06T20:59:34.428Z",
  		"otherFields": "willAlsoBeIncluded"
	}
	
You can also do multi level includes using dot notation. If you wanted to include the post for a comment and the post's author as well you can do:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'order=-createdAt' \
  	--data-urlencode 'limit=10' \
  	--data-urlencode 'include=post.author' \
  	<server_address>/1/classes/Comment

You can issue a query with multiple fields included by passing a comma-separated list of keys as the include parameter.

##Counting Objects##

If you are limiting your query, or if there are a very large number of results, and you want to know how many total results there are without returning them all, you can use the count parameter. For example, if you only care about the number of games played by a particular player:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'where={"playerName":"Jonathan Walsh"}' \
  	--data-urlencode 'count=1' \
  	--data-urlencode 'limit=0' \
  	<server_address>/1/classes/GameScore

Since this requests a count as well as limiting to zero results, there will be a count but no results in the response.

	{
  		"results": [],
  		"count": 1337
	}

With a nonzero limit, that request would return results as well as the count.

##Compound Queries##

If you want to find objects that match one of several queries, you can use $or operator, with a JSONArray as its value. For instance, if you want to find players with either have a lot of wins or a few wins, you can do:

	curl -X GET \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-G \
  	--data-urlencode 'where={"$or":[{"wins":{"$gt":150}},{"wins":{"$lt":5}}]}' \
  	<server_address>/1/classes/Player

Any other constraints on the query are also applied to the object returned, so you can add other constraints to queries with $or.
Note that we do not, however, support non-filtering constraints (e.g. limit, skip, sort, include) in the subqueries of the compound query.	