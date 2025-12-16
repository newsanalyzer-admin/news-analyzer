@echo off
echo Starting NewsAnalyzer Backend (Development Mode)
echo.
echo Databases should be running:
echo - PostgreSQL: localhost:5432
echo - Redis: localhost:6379
echo.
echo Starting Spring Boot...
echo.
set CONGRESS_API_KEY=nCXoCiQtZApeVuctKN6MG1BIQIRmNVhLzQ9UxVgY
echo CONGRESS_API_KEY=%CONGRESS_API_KEY%
mvnw.cmd spring-boot:run -Dspring.profiles.active=dev

pause
