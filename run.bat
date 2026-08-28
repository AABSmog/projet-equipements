@echo off
rem Launch Proj-Equipment-Micronaut (dev). MDP par defaut = Smogolem10! (application.yml).
rem Surcharge possible via EQUIPMENTS_DB_PASSWORD / EQUIPMENTS_DB_URL / PORT.
if "%EQUIPMENTS_DB_PASSWORD%"=="" set EQUIPMENTS_DB_PASSWORD=Smogolem10!
if "%EQUIPMENTS_DB_URL%"=="" set EQUIPMENTS_DB_URL=jdbc:postgresql://127.0.0.1:5432/Equipments
if "%EQUIPMENTS_DB_USER%"=="" set EQUIPMENTS_DB_USER=postgres
if "%EQUIPMENTS_DB_DDL%"=="" set EQUIPMENTS_DB_DDL=update
if "%PORT%"=="" set PORT=8080
if "%JAVA_HOME%"=="" set JAVA_HOME=C:\Program Files\Java\jdk-26.0.2
call ./gradlew.bat run --console=plain