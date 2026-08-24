@echo off
rem Launch Proj-Equipment-Micronaut against the real database.
rem Defaults are now baked into application.yml (127.0.0.1:5432/Equipments,
rem postgres/Smogolem10!, hbm2ddl validate). Override via these variables:
if "%DATASOURCES_DEFAULT_URL%"=="" set DATASOURCES_DEFAULT_URL=jdbc:postgresql://127.0.0.1:5432/Equipments
if "%DATASOURCES_DEFAULT_USERNAME%"=="" set DATASOURCES_DEFAULT_USERNAME=postgres
if "%DATASOURCES_DEFAULT_PASSWORD%"=="" set DATASOURCES_DEFAULT_PASSWORD=Smogolem10!
if "%EQUIPMENTS_DB_DDL%"=="" set EQUIPMENTS_DB_DDL=validate
if "%PORT%"=="" set PORT=8085
if "%JAVA_HOME%"=="" set JAVA_HOME=C:\Program Files\Java\jdk-26.0.2
call ./gradlew.bat run --console=plain