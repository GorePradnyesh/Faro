Faro
=============================

Requirements : 
* Java - 1.7+ ( Preferred version: 1.7 ) 
* Maven - 3.x +
* Set JAVA and M2 paths.


Build and Run Steps: 
```sh
# Clean compile the projects the 
mvn clean compile
```

```sh
# Run the Junit Tests
mvn test
````
NOTE: The functional tests are disabled. To run the functional tests for now you need to first run the the dev server in the standalone more as shown below. 

```sh
# Run the app server 
mvn appengine:devserver

# See also appengine:devserver_start, appengine:devserver_stop
```
