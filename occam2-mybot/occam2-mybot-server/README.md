# jimin-ecm

# Front-end development environment
## Installation
* https://nodejs.org/ko/
* https://yarnpkg.com/en/docs/install#windows-stable
* https://cli.vuejs.org/guide/installation.html


# How to deploy jOOQ
## Download
    http://www.jooq.org/download/versions
    User name (E-Mail) : kt.kim@mycorp.net
    Password (License) : A2H54-436T6-W1A5J-16V1G-X5U1I
    
    https://www.jooq.org/download/pro/jOOQ-3.11.12.zip?region=us

## Maven settings.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<settings>
  <servers>
    <server>
      <id>mycorp</id>
      <username>admin</username>
      <password>369Wkwks</password>
    </server>
  </servers>
</settings>
</pre>
```

## jOOQ deploy command
```
$ maven-deploy -u http://nexus.mycorp.net/repository/maven-releases/ -r mycorp
```
