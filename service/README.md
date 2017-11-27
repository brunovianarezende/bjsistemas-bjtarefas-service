How to Create a .deb package
============================

First create the `service/src/universal/conf/application.ini` file. Check
`service/src/universal/conf/application.ini.template` for a example. Sentry
DSN is needed; it can be found at Sentry project interface.

After application.ini is in place, just run the command:

`./sbt "project service" debian:packageBin`

The final deb file will be created at:

`./service/target/Tasks service_<version>_all.deb`

One can see what will be the generated structure by running:

`./sbt "project service" stage`

and then look at:

`./service/target/universal/stage/`

How to deploy
=============

run the command `sudo dpkg -i /path/to/deb/file` followed by
`sudo apt install -f` I any of the dependencies is not found. If you want to
make sure the dependencies aren't uninstalled when you uninstall the service,
you can manually run `sudo apt install <the dependency>` for each of such
dependencies (ubuntu will change the dependency status to 'manually
installed').