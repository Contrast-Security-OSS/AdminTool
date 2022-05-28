#!/bin/sh

JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_231.jdk/Contents/Home"
BUNDLE_JRE_HOME="/Users/XXXXX/Downloads/jre1.8.0_202.jre"
APP_NAME="AdminTool"
APP_VERSION="1.0.0"
APP_DIR_NAME="${APP_NAME}_${APP_VERSION}.app"

javapackager \
  -deploy -Bruntime=${BUNDLE_JRE_HOME} \
  -native image \
  -srcdir ./build/libs \
  -srcfiles ${APP_NAME}-${APP_VERSION}.jar \
  -outdir  ./build/libs \
  -outfile ${APP_DIR_NAME} \
  -appclass com.contrastsecurity.csvdltool.Main \
  -name "${APP_NAME}_${APP_VERSION}" \
  -title "${APP_NAME}" \
  -BjvmOptions=-XstartOnFirstThread \
  -BjvmOptions=-Xms128m \
  -BjvmOptions=-Xmx256m \
  -vendor "Contrast Security Japan G.K." \
  -Bicon=src/main/resources/csvdltool.icns \
  -Bmac.CFBundleVersion=${APP_VERSION} \
  -nosign \
  -v

echo ""
echo "If that succeeded, it created \"build/libs/bundles/${APP_DIR_NAME}\""

exit 0
