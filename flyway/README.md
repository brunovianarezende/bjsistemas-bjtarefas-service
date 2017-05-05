How to execute migrations
=========================

in command line do:

```DB_URL=<dburl> DB_USER=<user> DB_PASSWORD=<password> ./sbt "project flyway" flywayMigrate```

for example:

```DB_URL=jdbc:mysql://localhost/tasks_test DB_USER=task DB_PASSWORD=  ./sbt "project flyway" flywayMigrate```

in our build.sbt we already have the defaults:

* url - jdbc:mysql://localhost/tasks_test
* user - task
* password - <i>no password</i>

so the above example could have been rewritten as:

```DB_USER=task ./sbt "project flyway" flywayMigrate```

This way of running the migration requires the project to compile, otherwise sbt will complain.
If this is not desired, an option would be to run flyway from the command line directly, by
following what is described at:

<https://flywaydb.org/getstarted/firststeps/commandline>