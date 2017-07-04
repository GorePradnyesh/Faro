Faro
=============================

Requirements : 
* Java - 1.7+ ( Preferred version: 1.7 ) 
* Maven - 3.1 +
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

Backend Dev Environment Setup Instructions :

1. Install Java

  App engine requires Java 7 bytecode level.  
  Download and install JDK 7. 
  http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html

  JDK will get installed in the following location typically -

  /Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk


2. Download and extract the Google App engine SDK

   https://cloud.google.com/appengine/docs/java/download

   Extract to any directory and add it to your PATH as mentioned in the above link

3. Install IntelliJ IDEA Community Edition

   https://confluence.jetbrains.com/display/IntelliJIDEA/Previous+IntelliJ+IDEA+Releases

   Install the 15.x version. The latest version requires Java 8.


4. Setup IntelliJ IDEA for App engine 

   https://cloud.google.com/appengine/docs/java/tools/setting-up-intellij

   On this page, follow the instructions to Install the Cloud SDK and then Installing Cloud Tools for IntelliJ IDEA

5. Import the Faro Backend project into the IDE by browsing to the projects's pom.xml

   Faro/Backend/pom.xml 

   When importing the project, on the page that asks about project SDK, be sure to mention the Java JDK.

6. Download and configure maven 3.1 or above

   http://maven.apache.org/download.cgi

  - Extract to any directory 
  - Set JAVA_HOME to the location of your JDK

    export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home  

  - Set Maven HOME and add maven to your PATH
  
    export M2_HOME=/Users/granganathan/Development/apache-maven-3.3.9

You may build/run completely from IntelliJ as well, but make sure to specify the Maven home in intellij
Open IntelliJ Preferences, under Build Tools->Maven, set the Maven home directory

## Deploy to google cloud
```
mvn appengine:update -Dmaven.test.skip=true
```
