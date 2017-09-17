# Scala-Play-Boilerplate


# Libraries Used
- Java8
- Play Framework
- PostgreSQL
- Flyway
- Quill
- Enumeratum
- Circe
- Cats
- Shapeless


# Notes

## PostgreSQL in Docker

Run `docker-compose up -d` to launch a PostgreSQL instance for development. By default the database can be connected to 
at `localhost:27001`.

## Database Migration



### TODO
1. Integrate with sbt-docker perhaps??

## IntelliJ Idea Integration
[doc](https://playframework.com/documentation/2.6.3/IDE#Navigate-from-an-error-page-to-the-source-code)

Set up Play to add hyperlinks to an error page. This will link to runtime exceptions thrown when Play is running in 
development mode.

## [Coursier](https://github.com/coursier/coursier) Scala artifact fetching

Coursier is a dependency resolver / fetcher for Maven / Ivy. It can download artifacts in parallel.

## [Scala Formatter](https://github.com/scalameta/scalafmt)

A Scala formatter configuration file `.scalafmt.conf` is included in the project. Only git included files are formatted.

The sbt plugin, [neo-sbt-scalafmt](https://github.com/lucidsoftware/neo-sbt-scalafmt) is used to integrate formatter 
with sbt. For compatibility with `sbt 0.13`, a workaround is used in `build.sbt`.

To format source code manually, run `sbt scalafmt` to format source code.

## [Scala WartRemover](https://github.com/wartremover/wartremover)

At compile time, WartRemover

## How to start?

1. Install docker and docker-compose.
2. Run `docker-compose up -d` in root directory to launch an instance of PostgreSQL.
3. Create a file at `migration/src/main/scala/migrations/Summary.scala` with the following content
```scala
object MigrationSummary {

}
```
4. Run `sbt mg init` to initialize database migration. This will create a table `__migration__` for bookkeeping 
migration progress.
5. Run `sbt mg migrate` to migrate database.
6. Once migration is done, run `sbt mg codegen` to generate Slick table classes.
7. 

# TODO

1. Update cats to 1.0.0 when released.
2. Update circe to 1.0.0 when its dependency on cats is updated.
3. Update slick-pg's when circe is updated.
4. Dev, test, prod environment.
5. Test code.
6. Hikari config.
7. Figure out a way to no longer require user to create `Summary.scala` file manually after cloning new project.
