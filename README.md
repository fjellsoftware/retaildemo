Loppi retail demo
====================

[Loppi](https://loppi.io) is a software library that among other things aims to simplify development of GraphQL APIs,
and this demo showcases how the APIs and source code 
might look like if one were to use Loppi to implement an e-commerce backend. A live running instance of this project 
can be found at [retail-demo.loppi.io](https://retail-demo.loppi.io).

### **Features and access control**

There are several features available depending on whether you are logged in, and when you are logged in, 
what your role is.

- **Unauthenticated users**. When you are not logged in you can still do several things: log in, sign up, view available 
products, and place orders as a guest. Products can be filtered and ordered by various attributes like price, 
date, description or id. All of these features are available through the customer API.

- **Sign up and log in**. After signing up, 
your user will be given the role customer by default. When you are logged in as a customer, you can do all the things 
that unauthenticated users can do and more: You can place orders and have them registered on your account, view your 
orders and logout. For demo purposes you can also upgrade your user to a staff role.

- **Staff users**. In order to authenticate as a staff user, you must first create a regular account and then 
upgrade it to a staff role. This is not a feature that a production application should have available to customers, 
but here it lets you check out the features otherwise available only to staff members. First of all, when you are authenticated 
as a staff member, you or the client that you are using can fetch the schema of the staff API. Regular customers are 
not even allowed to view what staff user can do. As for the features available through this API, 
there are several: get all orders with order lines from all users, get all login sessions from all users, get all 
users, mark orders as "shipped" or "in progress" and lastly, create and update new products.

All the features described can also be seen if you look at the GrahpQL schema for each api. 
Additionally, there are several features included in this 
project that are not so visible to the end-user, like rate-limiting, logging and metrics gathering. Since this is a 
demo, you do not pay for the orders that you place, and naturally you do not receive any physical packages either. 
Similarly, the demo does not integrate with email, so features that require functional email like password-recovery 
are not implemented.

One key aspect of this demo is that anyone can become a staff member, and access all features available to one. 
For example staff members can view all users, their names and their orders, with addresses, phone numbers and so on. 
Thus, steps have been taken in order to protect your privacy, mainly by preventing you from submitting personal data 
in the first place, so if you try to use the API in various creative ways, you may get a response that says you need 
to input specific data. For example all users are called "John Doe", they all have similar, random-looking usernames, 
and all orders are ordered to the address "123 Maple Street, Anytown". This is also to allows the demo to run without 
anyone having to moderate user input. At any rate, feel free to play around and in general you don't need to be afraid 
of doing something wrong.

The main data used is based on an anonymized public dataset from a real-world e-commerce store. Data you submit will 
be deleted after 4 hours, that includes your account data as well. Some of your data may be deleted earlier if it 
depends on other user-submitted data.

### **Code architecture**
If you want to get an overview of the codebase by browsing the code itself, a good place to begin is Application.java.
It initializes the dependencies and starts the server, and most things in the project can be found if you start there 
and follow method calls to other classes. The only features that can not be found this way are the CLI and the 
logger initialization, which are found in Main.java and LoggerInitializer.java respectively, as well as the development 
tools found in the development package.

In the root package (src/main/java/com/fjellsoftware/retaildemo) are a number of classes and packages. 
The classes in the root package are mostly related to infrastructure and setting up dependencies.
Each of the packages within the root package have a category regarding what kind of classes they contain, and here we go through each of them.

- **graphqlexecutor**. When API requests arrive at the server initiated in Application.java, they are wired to one of 
the classes here. There is an API for customers, and one for staff users, and there is a separate executor for each. 
These executors call logic from the classes in the domain package, depending on which fields were included in the request.
- **domain**. Most domain/business logic for the API is stored here. 
Typically, there is one class for each database table indirectly exposed via the API. 
If something is wrong with a specific field in the API, the best place to start looking for a bug should be in one of these classes.
- **development and autogenerated**. The development package contains the classes used during development to call Loppi's
class generators, and the generated classes are stored in the autogenerated package.
- **authorizationcommon**. In this package there are various classes related to authorization that didn't fit in any other package.
Here we find a few structures/records holding authorization information, rate limiting and captcha verifier among other things.
Note that sign up and login are not here but in the domain package.
- **demo**. Since this project is meant to demonstrate Loppi's capabilities there are quite a few things done in this 
project that you would not normally do in a production environment. 
Several of these things are spread out across various classes in the domain package, but most of it is contained here.
- **util**. This is a package for various utilities.

### **Build and run**
If you want to build this project, firstly you need a copy of Loppi in order to fulfill the dependency 
requirement. Secondly, you need java 21 and a version of maven that supports it, for example 3.9.2.
In order to run the project, you need a local postgres database called "retail_demo" and a postgres user called 
"application" with read/write access to the tables and sequences in this database. 
The database should have schema equal to the schema.sql file in the project root directory. 
When running the tests, it is expected that a file called "database_secret" is present in the user home directory of the current user, 
where the file contains one line that is the password for the database user. If the CLI is used instead by executing 
the .jar file we soon create, you can set the -c/--credentials option to set the directory for where to search for credential files. 

When all the dependencies and infrastructure is set up, to build it, navigate to the project root directory and run:
```
mvn package
```
That should create two .jar files under the target directory in the project root folder.
If you run the fat jar, you can use the --help option to see what options are available:
```
java -jar ./target/retail-demo-[version]-jar-with-dependencies.jar --help
```
Additionally, there is an example deployment service file, for use with systemd, 
in the deployment directory in the root project file.

