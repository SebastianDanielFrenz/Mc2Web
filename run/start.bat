:loop
cd C:\eclipseApps\Mc2Web\run
java -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -Xms512M -Xmx2G -XX:MaxPermSize=128M -jar "C:\javaLib\spigot\spigot-1.14.2.jar"
timeout -t 10
goto loop