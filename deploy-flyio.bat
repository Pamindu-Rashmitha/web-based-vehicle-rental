@echo off
echo ========================================
echo Fly.io Deployment Helper Script
echo ========================================
echo.

echo This script will help you deploy your application to Fly.io
echo.

:menu
echo Choose an option:
echo 1. Install flyctl (first time only)
echo 2. Login to Fly.io
echo 3. Set up secrets
echo 4. Deploy application
echo 5. View logs
echo 6. Open application
echo 7. Check status
echo 8. SSH into application
echo 9. Exit
echo.
set /p choice="Enter your choice (1-9): "

if "%choice%"=="1" goto install
if "%choice%"=="2" goto login
if "%choice%"=="3" goto secrets
if "%choice%"=="4" goto deploy
if "%choice%"=="5" goto logs
if "%choice%"=="6" goto open
if "%choice%"=="7" goto status
if "%choice%"=="8" goto ssh
if "%choice%"=="9" goto end
goto menu

:install
echo.
echo Installing flyctl...
powershell -Command "iwr https://fly.io/install.ps1 -useb | iex"
echo.
echo Installation complete! Please restart this script.
pause
goto menu

:login
echo.
echo Logging in to Fly.io...
flyctl auth login
echo.
pause
goto menu

:secrets
echo.
echo Setting up secrets...
echo.
echo Please enter your configuration values:
echo.

set /p db_url="Database URL (e.g., jdbc:mysql://host:3306/vehicle_rental): "
set /p db_user="Database Username: "
set /p db_pass="Database Password: "
set /p mail_user="Email Username: "
set /p mail_pass="Email Password (App Password): "
set /p stripe_secret="Stripe Secret Key (or press Enter to skip): "
set /p stripe_pub="Stripe Publishable Key (or press Enter to skip): "

echo.
echo Setting secrets...
flyctl secrets set DATABASE_URL="%db_url%"
flyctl secrets set DATABASE_USERNAME="%db_user%"
flyctl secrets set DATABASE_PASSWORD="%db_pass%"
flyctl secrets set MAIL_USERNAME="%mail_user%"
flyctl secrets set MAIL_PASSWORD="%mail_pass%"

if not "%stripe_secret%"=="" (
    flyctl secrets set STRIPE_SECRET_KEY="%stripe_secret%"
)

if not "%stripe_pub%"=="" (
    flyctl secrets set STRIPE_PUBLISHABLE_KEY="%stripe_pub%"
)

echo.
echo Secrets configured successfully!
pause
goto menu

:deploy
echo.
echo Deploying application to Fly.io...
echo This may take several minutes...
echo.
flyctl deploy
echo.
echo Deployment complete!
pause
goto menu

:logs
echo.
echo Fetching logs...
flyctl logs
pause
goto menu

:open
echo.
echo Opening application in browser...
flyctl open
pause
goto menu

:status
echo.
echo Checking application status...
flyctl status
echo.
pause
goto menu

:ssh
echo.
echo Connecting to application via SSH...
flyctl ssh console
pause
goto menu

:end
echo.
echo Goodbye!
exit
