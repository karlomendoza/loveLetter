# Love letter online
This is a version of the card game love letter, it uses websockets to distribute the game state to each player.
Actions are send to the server as a JSon. 


## Used languages: ##

* JDK 1.8
* Maven 3
* Apache Tomcat 7.0.29
* jQuery 1.7.2
* jQuery-UI 1.8.22

## How to use it ##

Do a mvn clean install
Copy the websockets.war from the target/ directory to your tomcat 7 webapps/ directory
Start tomcat 7
Go to http://localhost:8080/websockets/

Select a name, click on connect, and when other players join, click on new game.

## How to play the game ##
https://youtu.be/k2YUYPDq7gQ?t=1m