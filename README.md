# NBS Deduplication API
This is repository for patient deduplication in NBS produced by Georgia Tech capstone team 9345.


# Installation
The only dependencies which need to be manually installed are
* Java (Version 8+)
* [Gradle](https://gradle.org/)
* [Node.js](https://nodejs.org/en/)

Gradle and npm (which is bundled with node.js) will take care of installing other dependencies.

To verify that you can build the API and UI correctly
* run `gradle build` from the `api` folder
* run `npm install` from the `ui` folder

# User Interface
The demo UI, which shows what we envision would ultimately be integrated into NBS, is located in the `frontend` subdirectory.

To start the Demo UI
1. Make sure the Node project is set up by running `npm install` from the `frontend` directory (this need only be done once)
2. Start the backend server by running `./gradlew bootRun` (on Linux/OSX) or `gradlew.bat bootRun` (on Windows) from the `api` directory. This will start the server on `localhost:8080/`: the CLI loading bar will only ever get to 75%, that's fine.
3. Start the user interface by running `npm start` from the `frontend` directory. This will start the user interface on `localhost:3000/`, and should automatically open it in your browser. Otherwise, you can always manually enter the url.

# Deduplication API
The back-end API is the largest component of the project, and is what would ultimately be integrated into the NBS. It includes functions meant to accessed through pure Java and  a limited REST api which can be used to access some of the API's functionality. What follows is a broad description of the API's functionality and the most relevant technical components.

**Note**: If you are trying to open the Java project in an IDE, open the `dedup/api` folder specifically: not the `dedup` folder

## Code Documentation
Almost every function in the codebase is annotated with comments and JavaDoc'd, and human-readable documentation can be generated in HTML format using the `gradle javadoc` command

## Tests
Some tests which verify the functionality of the API and show how to use it are provided in the `dedup/src/main/test` directory. `DeduplicationTest.java` most clearly illustrates the core functionality of our API.

Some of the tests use an in-memory mock database so that we can control the testing data, but others are set up to connect to an actual NBS database. For the latter to successfully run, you will need to edit the authentication information in `src/Constants/Config.java`

The tests can be run via a simple `gradle test`, or through your IDE of choice

## Deduplication Mechanics
The Deduplication API is capable of detecting sets of records in an NBS database which match on any specified set of fields.

Its functionality is best illustrated by example. Take for instance this following (abstract) NBS database
```
# Person Table
[
    {"person_uid": 1, "first_nm": "John", "last_nm": "Doe", "SSN": null, ...},
    {"person_uid": 2, "first_nm": "Jon", "last_nm": "Doe", "SSN": "999-99-9999", ...},
    {"person_uid": 3, "first_nm": "John", "last_nm": "Doe", "SSN": "999-99-9999", ...},
    {"person_uid": 4, "first_nm": "Venkat", "last_nm": "Kumar", "SSN": null, ...},
    {"person_uid": 5, "first_nm": "Dosi", "last_nm": "Doe", "SSN": null, ...},
    {"person_uid": 6, "first_nm": "Barack", "last_nm": "Obama", "SSN": "444-44-4444", ...},
    ...
]
# Person_name Table
[
    {"person_uid": 1, "first_nm": "John", "last_nm": "Doe", "fname_soundex": "A32", ...},
    {"person_uid": 2, "first_nm": "Jon", "last_nm": "Doe", "fname_soundex": "A32", ...},
    {"person_uid": 4, "first_nm": "Venkat", "last_nm": "Kumar", "fname_soundex": "B49" ...},
    {"person_uid": 6, "first_nm": "Barack", "last_nm": "Obama", "fname_soundex": "Q02",
    ...
]
```

And consider the problem of trying to find the records which match on both first and last name (what we call the "subconfig"  `[first_name, last_name]`).

With *`N`* items in the database, then the complexity of comparing every record to every other record is *`O(N^2)`*, much too slow for large databases. We implement a hash-based solution where every record is hashed using the fields in the requested configuration, and HashSets are used to detect duplicates in `O(N)` time, the best theoretically possible.

In the subconfig above, the database might be processed to form the following *Aux Map* (a term which will come up a lot).
```
{
    attributes: [first_name, last_name],
    uid_to_hashes: {
        1: 0x75,
        2: 0x02,
        3: 0x75,
        4: 0x32, 5: 0x77, 0x10
    }
    hashes_to_uids: {
	0x75: [1, 3],
	0x02: [2]
	0x32: [4], 0x77: [5], 0x10: [6]
    }
}
```

After the AuxMap is constructed, groups of duplicates can quickly be read off from the `hashes_to_uids` field. In this case, we can verify that the records with IDs 1 and 3 have the same first and last names

### Composition of Subconfigs
The deduplication functions actually never take individual subconfigs. Instead, they take in *lists* of subconfigs: such a list is called a *configuration* (the terminology isn't exactly great :frowning:).

Doing things this way allow subconfigs to be composed to get more complicated logic. A complete technical explanation for how the composition works is as follows

1. Two records match under a *subconfig* if their values on **all** of the constituent *match fields* (things like "first name", or "SSN"), are equal
2. Two records match under a *config* if they match on **any** of the config's constituent *subconfigs*

Again, examples are more illustrative. Let's say that we wanted to deduplicate based on a very strict *Same* configuration: we might do so by making the following config
```
[
    [first_name, last_name, ssn, address, birthday]
]
```
Two records would have to match on *every single one* of the above fields to be considered matching: so for instance the two record here
```
{"uid": 30, first_name: "John", last_name: Doe, ssn: 999-99-999, address: "12 World Street", bday: "03-03-03"}
{"uid": 200, first_name: "John", last_name: Doe, ssn: 999-99-999, address: "13 World Street", bday: "03-03-03"}
```

**would not** be considered matches: they differ on address.

That's a little strict though. We might consider SSN a sufficient qualifier for matches, or if the first & last names and birthday match, then that might be good enough too. We could thus use deduplicate on following *config*
```
[
    [ssn,],
    [first_name, last_name, birthday]
]
```
which, on the following database
```
{"uid": 30, first_name: "John", last_name: Doe, ssn: 999-99-999, address: "12 World Street", bday: "03-03-03"}
{"uid": 200, first_name: "John", last_name: Doe, ssn: 999-99-999, address: "13 World Street", bday: "03-03-03"}
{"uid": 400, first_name: "Hello", last_name: "World", ssn: null, address: "Georgia Tech", bday: null}
{"uid": 1000, first_name: "Sherlock", last_name: "Holmes", ssn: "123-45-6789", address: "221B Baker Street", bday: "01-01-1998"}
{"uid": 999, first_name: "Totally", last_name: "Unique", ssn: "record", address: "without" bday: "exception"}
```
would return the following grouping of records `[[30, 200], [400, 1000], [999]].`

### Note on Unknown Values
It's worth mentioning that for a record to appear in a certain AuxMap (which again, stores hashes for *subconfigs*), it must have known values for **all** of the the match fields described for the Aux Map.

For instance, this record
```
{"uid": 400, first_name: "Hello", last_name: "World", ssn: null, ...}
```

does not affect the AuxMap tracking `[first_name, ssn]` *at all*. It would, however affect the separate AuxMap tracking `[first_name, last_name]`

This is as opposed to substituting special constants for unknown values: this alternate solution would cause undesirable behavior like any two records with unknown SSNs matching under the subconfig `[SSN]`.

## Aux Map Management

It's very inefficient to traverse the entire NBS database and rehash things every time you want to use the given subconfig to deduplicate. Most of the data in the database won't change between deduplication runs, and the bigger the database is the longer it'll take to construct an AuxMap.

For this reason, the Deduplication API caches aux maps on disk. The AuxMapManager class takes care of managing local AuxMaps, and provides hooks to update the database when records are added to or removed from the NBS database. Using the hooks prevents unnecessary full traversals of the database<sup>1</sup>

# Deduplication Server & REST API
Some of the Deduplication API's functionality can be exposed through REST endpoints by launching our API's [Spring Boot](https://spring.io/projects/spring-boot) (see the UI section).

Importantly, this server is **INSECURE**. It does not implement any form of authentication, and will be exposed over the local network  if the host machine is. Although it is not a data or privacy risk, since the endpoints never expose anything but "person_uid" values, it still should not be used as-is in a production environment.

The Server & REST API's main purpose is to facilitate the demo UI's communication with the Deduplication API, but might be useful in the future (after being secured) if networked access to deduplication functionality is desired.

The existing endpoints are all implemented and documented in the `server` package. Usage might not be entirely clear just from the documentation however, so functionaly example usages/ouputs are provided as a [Postman](https://www.postman.com/) project in the `postman` directory.

The arguments to in the Postman requests might look funky, which is due to their being encoded as URI components. To encode/unencode them, select the parameter value, highlight all, right click, and select the relevant "Encode/Decode URI" option.

# Incomplete & Future Work
1. [High Priority | Medium Difficulty] Currently, AuxMap files are loaded, modified, and saved every single time `hookAddRecord` and `hookRemoveRecord` are called (which is every time a record is added/removed).

   However file operations are expensive, and using SQL queries to retrieve single records is also inefficient. Instead, the hooks should modify a job queue of things to add & delete, which would be flushed every time the `getAuxMap` function was called or the Java process was shut down.

   The current implementation works for databases which are rarely modified, but the faster version will need to be implemented for realistic databases.

2. [High Priority | Medium Difficult] As discussed in our last meeting, our API composes attributes from different tables incorrectly. This behavior is controlled through the `getSQLQueryForEntries` function in `SQLQueryUtils.java`. The column aliasing logic, which comprises the first half of the method, should be fine: but the second half, which controls how tables are joined together, should be updated to implement the correct kind of table join.

3. [Medium Priority | Low Difficulty] Not all of the requested match fields are implemented in `MatchFieldEnum.java` right now. This means that the codebase isn't set up to deduplicate based on email, among other things.
Fortunately, this is only a matter of writing some easy boilerplate code in `MatchFieldEnum.java`: the API is intended to be extended in this way. The already-implemented Match Fields should provide ample example of how to do so.

# Authors
- Daniel Finkelstein
- Christian Gutowski
- Winston Li
- Anish Moorthy (anlsh@protonmail.com)
- Lewis Vaughan
