# --- Stage 1: مرحلة بناء التطبيق وتحميل المكتبات ---
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# 1. نسخ ملف الـ pom.xml أولاً لتحميل المكتبات وكاش الطلبات لتسريع الـ Build مستقبلاً
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. نسخ مجلد الكود المصدري (Source Code) وبناء ملف الـ JAR
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: مرحلة التشغيل النهائية ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 3. إنشاء مستخدم مخصص غير جذر (Non-root user) لحماية الحاوية داخل الـ Kubernetes Cluster
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# 4. نسخ ملف الـ JAR التنفيذي فقط من المرحلة الأولى (تجاهل كل ملفات الـ Maven والـ Source الكبيرة)
COPY --from=builder /app/target/*.jar app.jar

# 5. تحديد المنفذ الافتراضي للـ UserMS
EXPOSE 8080

# 6. إعدادات الـ JVM للتشغيل بكفاءة وإنهاء الحاوية فوراً في حال حدوث خطأ في الذاكرة (Out Of Memory)
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]