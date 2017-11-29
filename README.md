How to deploy
=============

After all the steps bellow are done, the service will be running in port 8000.

1. Get the deb file for the project (see `./service/README.md` for how to
generate it)

2. Install the deb file

There are two options to install the deb file:

 - (preferred) use GDebi Package Installer

 - run the command `sudo dpkg -i /path/to/deb/file` followed by
`sudo apt install -f` I any of the dependencies is not found. If you want to
make sure the dependencies aren't uninstalled when you uninstall the service,
you can manually run `sudo apt install <the dependency>` for each of such
dependencies (ubuntu will change the dependency status to 'manually
installed').

3. After the deb file is installed, one must create the database user and the
database

In mysql prompt, do:

```
CREATE DATABASE tasks;
CREATE USER 'task'@'localhost';
GRANT ALL ON tasks.* TO 'task'@'localhost';
```

3. install flyway command line

Assuming java is already installed and in the path, just download
https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/4.2.0/flyway-commandline-4.2.0.tar.gz, 
uncompress and put the resulting dir in the PATH

4. get the migrations (see `./flyway/README.md`for how to generate it)

5. execute the migrations (assume flyway is in the path and the
migrations will be in the _migrations_ dir):

`flyway -url=jdbc:mysql://localhost/tasks -user=task -locations=filesystem:$(pwd)/migration migrate`
