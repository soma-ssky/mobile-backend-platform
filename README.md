#Mobile Backend Platform Menual#


##REST API##

| address | method | function |
|---|:---:|---|
| /(version)/classes/(className) | POST | Creating Objects  | 
| /(version)/classes/(className)/(objectId) | GET | Retrieving(Fetching) Objects
| /(version)/classes/(className)/(objectId) | PUT | Updating Objects
| /(version)/classes/(className) | GET | Querying(Getting) Object
| /(version)/classes/(className)/(objectId) | DELETE | Deleting Objects
| /(version)/batch | POST | Batch Operation

currently version is 1


##Creating Objects##

To create a new object on Parse, send a POST request to the class URL containing the contents of the object. For example, to create the object described above:

###Request###

    curl -X POST \
    -H "Content-Type: application/json" \
    -d '{"score":1337,"playerName":"Sean Plott","cheatMode":false}' \
    http://<server_address>/1/classes/GameScore

When the creation is successful, the HTTP response is a 201 Created and the Location header contains the object URL for the new object:

###Header of Result###

    Status: 201 Created
    Location: http://<server_address>/1/classes/GameScore/Ed1nuqPvcm
    The response body is a JSON object containing the objectId and the createdAt timestamp of the newly-created object:

###Body of Result###

    {
        "createdAt": "2011-08-20T02:06:57.931Z",
        "objectId": "e34f189e-f910-4a28-9eb7-3bc85a383eee"
    }


##Retrieving Objects##

Once you've created an object, you can retrieve its contents by sending a GET request to the object URL returned in the location header. For example, to retrieve the object we created above:

###Request###

    curl -X GET \
    -H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
    -H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
    http://<server_address>/1/classes/GameScore/Ed1nuqPvcm


The response body is a JSON object containing all the user-provided fields, plus the createdAt, updatedAt, and objectId fields:

###Body of Result###

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

###Request###

    curl -X GET \
        -H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
        -H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
        -G \
        --data-urlencode 'include=game' \
        http://<server_address>/1/classes/GameScore/Ed1nuqPvcm


###Updating Objects###

To change the data on an object that already exists, send a PUT request to the object URL. Any keys you don't specify will remain unchanged, so you can update just a subset of the object's data. For example, if we wanted to change the score field of our object:

###Request###

    curl -X PUT \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-H "Content-Type: application/json" \
  	-d '{"score":73453}' \
  	http://<server_address>/1/classes/GameScore/Ed1nuqPvcm

The response body is a JSON object containing just an updatedAt field with the timestamp of the update.

###Result###

	{
  		"updatedAt": "2011-08-21T18:02:52.248Z"
	}


###Counters###

To help with storing counter-type data, Parse provides the ability to atomically increment (or decrement) any number field. So, we can increment the score field like so:

###Request###

	curl -X PUT \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-H "Content-Type: application/json" \
  	-d '{"score":{"__op":"Increment","amount":1}}' \
  	http://<server_address>/1/classes/GameScore/Ed1nuqPvcm


###Arrays###

To help with storing array data, there are three operations that can be used to atomically change an array field:

Add appends the given array of objects to the end of an array field.
AddUnique adds only the given objects which aren't already contained in an array field to that field. The position of the insert is not guaranteed.
Remove removes all instances of each given object from an array field.
Each method takes an array of objects to add or remove in the "objects" key. For example, we can add items to the set-like "skills" field like so:

###Request###

	curl -X PUT \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-H "Content-Type: application/json" \
  	-d '{"skills":{"__op":"AddUnique","objects":["flying","kungfu"]}}' \
  	http://<server_address>/1/classes/GameScore/Ed1nuqPvcm


###Relations###

In order to update Relation types, Parse provides special operators to atomically add and remove objects to a relation. So, we can add an object to a relation like so:

###Request###

	curl -X PUT \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-H "Content-Type: application/json" \
  	-d '{"opponents":{"__op":"AddRelation","objects":[{"__type":"Pointer","className":"Player","objectId":"Vx4nudeWn"}]}}' \
  	http://<server_address>/1/classes/GameScore/Ed1nuqPvcm


To remove an object from a relation, you can do:

###Request###

	curl -X PUT \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-H "Content-Type: application/json" \
  	-d '{"opponents":{"__op":"RemoveRelation","objects":[{"__type":"Pointer","className":"Player","objectId":"Vx4nudeWn"}]}}' \
  	http://<server_address>/1/classes/GameScore/Ed1nuqPvcm


###Deleting Objects###

To delete an object from the Parse Cloud, send a DELETE request to its object URL. For example:

###Request###

	curl -X DELETE \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	http://<server_address>/1/classes/GameScore/Ed1nuqPvcm

You can delete a single field from an object by using the Delete operation:

###Request###

	curl -X PUT \
  	-H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \
  	-H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \
  	-H "Content-Type: application/json" \
  	-d '{"opponents":{"__op":"Delete"}}' \
  	http://<server_address>/1/classes/GameScore/Ed1nuqPvcm


##Batch Operations##

To reduce the amount of time spent on network round trips, you can create, update, or delete several objects in one call, using the batch endpoint.

Each command in a batch has method, path, and body parameters that specify the HTTP command that would normally be used for that command. The commands are run in the order they are given. For example, to create a couple of GameScore objects:

###Request###

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
  	http://<server_address>/1/batch
  	
The response from batch will be a list with the same number of elements as the input list. Each item in the list with be a dictionary with either the success or error field set. The value of success will be the normal response to the equivalent REST command:

###Result of Body###

	{
  		"success": {
    		"createdAt": "2012-06-15T16:59:11.276Z",
    		"objectId": "YAfSAWwXbL"
  		}
	}

The value of error will be an object with a numeric code and error string:

###Result of Body###

	{
  		"error": {
    		"code": 101,
    		"error": "object not found for delete"
  		}
	}

Other commands that work in a batch are update and delete.

###Request###

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
	http://<server_address>/1/batch