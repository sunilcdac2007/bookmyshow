# Base Image:
FROM registry-dev.uidai.gov.in:5000/centos-java8-wkhtmltopdf-locale:v1
ARG version



# Labels:
LABEL maintainter="unifiedApp_order_pvc_reprint-v1.1.4@uidai.gov.in"

# Installing Application:
COPY  ./target/Unified_App_Order_PVC_Reprint-*.jar /opt/uidapp/UA/Unified_App_Order_PVC_Reprint-${version}.jar
COPY ./fonts /usr/share/fonts

# Port
EXPOSE 9034

# Startup Script
#CMD java -jar /opt/uidapp/UA/Unified_App_Order_Reprint-*.jar --spring.config.location=/storage1/portal-QA/unifiedApp_order_reprint/config/application.properties > /storage1/portal-QA/unifiedApp_order_reprint/logs/unifiedApp_order_reprint-log.txt
