This is Marco's Syslog Valve for Apache Tomcat, fixed such that it works with tomcat7.
See http://marcoscorner.walther-family.org/2012/06/apache-tomcat-and-logging-the-2nd/ for more details.

Additional parameters from this fork:
- `port`: Syslog UDP target port
- `msgLength`: UDP packet message length to be sent to syslog
  > - Maximum: 65507
  > - Minimum: 480

### Example:  
```
<Valve className="org.apache.catalina.valves.SyslogAccessLogValve"
	requestAttributesEnabled="true"
	hostname="localhost"
	port="514"
	msgLength="32766"
	resolveHosts="false"
	pattern="%h %l %u %t &quot;%r&quot; %s %b" />
```
