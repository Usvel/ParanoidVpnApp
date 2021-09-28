@ECHO OFF
TITLE Check style with Checkstyle
SET ROOT_FOLDER=build\bin
SET VERSION=8.36.1
SET CHECKSTYLE_BIN=%ROOT_FOLDER%\checkstyle-%VERSION%.jar
if not exist %CHECKSTYLE_BIN% (
  ECHO Please wait, first download...
  if not exist %ROOT_FOLDER% MKDIR %ROOT_FOLDER%
  curl.exe -sL "https://github.com/checkstyle/checkstyle/releases/download/checkstyle-%VERSION%/checkstyle-%VERSION%-all.jar" -o "%CHECKSTYLE_BIN%"
)

java -jar %CHECKSTYLE_BIN% ./ -c .github/workflows/assets/android_checkstyle.xml %*
echo Done!
PAUSE
