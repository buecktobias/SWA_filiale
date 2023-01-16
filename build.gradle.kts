@file:Suppress("MissingPackageDeclaration", "SpellCheckingInspection", "GrazieInspection")

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import net.ltgt.gradle.errorprone.errorprone
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.asciidoctor.gradle.jvm.pdf.AsciidoctorPdfTask
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun
// TODO https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION") plugins {
    java
    idea
    checkstyle
    jacoco
    `project-report`

    alias(libs.plugins.springBoot)
    alias(libs.plugins.errorpronePlugin)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.testLogger)
    alias(libs.plugins.allure)
    alias(libs.plugins.sweeney)
    alias(libs.plugins.owaspDependencycheck)
    alias(libs.plugins.snyk)
    alias(libs.plugins.asciidoctor)
    alias(libs.plugins.asciidoctorPdf)
    alias(libs.plugins.nwillc)
    alias(libs.plugins.benManes)
    alias(libs.plugins.markelliot)
    alias(libs.plugins.dependencyAnalysis)
    alias(libs.plugins.licenseReport)
}
defaultTasks = mutableListOf("compileTestJava")
group = "com.acme"
version = "1.0.0"
sweeney {
    enforce(mapOf("type" to "gradle", "expect" to "[7.0,8.0]"))
    enforce(mapOf("type" to "jdk", "expect" to "[17.0.1,20]"))
    validate()
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaVersion.get()))
    }
}
repositories {
    mavenCentral()
    maven("https://repo.spring.io/release") { mavenContent { releasesOnly() } }
    maven("https://repo.spring.io/milestone") { mavenContent { releasesOnly() } }
}
configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}
/* ktlint-disable comment-spacing */
@Suppress("CommentSpacing")
dependencies {
    implementation(platform(libs.mockitoBom))
    implementation(platform(libs.allureBom))
    implementation(platform(libs.springBootBom))
    implementation(platform(libs.springdocOpenapiBom))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-tomcat")
    implementation(libs.tomcatJakartaeeMigration)
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.hibernateJpamodelgen)
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("mysql:mysql-connector-java")
    runtimeOnly("com.oracle.database.jdbc:ojdbc11")
    runtimeOnly(libs.jansi)
    runtimeOnly(libs.bouncycastle)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    compileOnly(libs.spotbugsAnnotations)
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.hamcrest", module = "hamcrest")
        exclude(group = "org.skyscreamer", module = "jsonassert")
        exclude(group = "org.xmlunit", module = "xmlunit-core")
    }
    testImplementation(libs.junitPlatformSuiteApi)
    testRuntimeOnly(libs.junitPlatformSuiteEngine)
    testImplementation("org.mockito:mockito-inline")
    errorprone(libs.errorprone)
    constraints {
        implementation(libs.annotations)
        implementation(libs.tomcatCore)
        implementation(libs.tomcatEl)
        implementation(libs.slf4jApi)
        implementation(libs.slf4jJul)
    }
}
tasks.compileJava {
    options.isDeprecation = true
    with(options.compilerArgs) {
        add("-Xlint:unchecked")
        add("--enable-preview")
        add("--add-opens")
        add("--add-exports")
    }
    with(options.errorprone.errorproneArgs) {
        add("-Xep:InvalidParam:OFF")
        add("-Xep:MissingSummary:OFF")
    }
}
tasks.compileTestJava {
    options.isDeprecation = true
    with(options.compilerArgs) {
        add("-Xlint:unchecked")
        add("--enable-preview")
    }
    options.errorprone.errorproneArgs.add("-Xep:VariableNameSameAsType:OFF")
}
tasks.named<BootJar>("bootJar") {
    doLast {
        println("")
        println("Aufruf der ausfuehrbaren JAR-Datei:")
        @Suppress("MaxLineLength") println(
            "java -D'LOG_PATH=./build/log' -D'javax.net.ssl.trustStore=./src/main/resources/truststore.p12' -D'javax.net.ssl.trustStorePassword=zimmermann' -jar build/libs/${archiveFileName.get()} --spring.profiles.default=dev --spring.profiles.active=dev [--debug]", // ktlint-disable max-line-length
        )
        println("")
    }
}
tasks.named<BootBuildImage>("bootBuildImage") {
    val path = "juergenzimmermann"
    imageName.set("$path/${project.name}")
    val tag = System.getProperty("tag") ?: project.version.toString()
    tags.set(mutableListOf("$path/${project.name}:$tag"))
    @Suppress("StringLiteralDuplication") environment.set(
        mapOf(
            "BP_JVM_VERSION" to "19.0.1",
            "BPL_JVM_THREAD_COUNT" to "20",
            "BPE_DELIM_JAVA_TOOL_OPTIONS" to " ",
            "BPE_APPEND_JAVA_TOOL_OPTIONS" to "--enable-preview",
            )
    )
}
tasks.named<BootRun>("bootRun") {
    jvmArgs("--enable-preview")
    val port = System.getProperty("port")
    if (port != null) {
        systemProperty("server.port", port)
    }
    val tls = System.getProperty("tls")
    if (tls == "false" || tls == "FALSE") {
        @Suppress("StringLiteralDuplication") systemProperty("server.ssl.enabled", "false")
        @Suppress("StringLiteralDuplication") systemProperty("server.http2.enabled", "false")
    }
    systemProperty("spring.profiles.default", "dev")
    systemProperty("spring.profiles.active", "dev")
    systemProperty("spring.output.ansi.enabled", "ALWAYS")
    systemProperty("spring.config.location", "classpath:/application.yml")
    systemProperty("server.tomcat.basedir", "./build/tomcat")
    systemProperty("server.ssl.client-auth", "NONE")
    systemProperty("LOG_PATH", "./build/log")
    systemProperty("APP_DB_PASSWORD", "p")
    systemProperty("APPLICATION_LOGLEVEL", "DEBUG")
    systemProperty("REQUEST_RESPONSE_LOGLEVEL", "TRACE")
    systemProperty("HIBERNATE_LOGLEVEL", "DEBUG")
    systemProperty("spring.datasource.password", "p")
    val persistenceProp = "spring.jpa.properties.jakarta.persistence"
    val schemaGenProp = "$persistenceProp.schema-generation"
    val db = System.getProperty("db")
    if (db == "mysql" || db == "oracle") {
        val dbUrl = when (db) {
            "mysql" -> "jdbc:mysql://localhost/filiale"
            "oracle" -> "jdbc:oracle:thin:@localhost/XEPDB1"
            else -> throw IllegalStateException("Fehler bei der Gradle-Option -Ddb=mysql oder -Ddb=oracle")
        }
        systemProperty("spring.datasource.url", dbUrl)
        systemProperty("$schemaGenProp.drop-script-source", "$db/drop.sql")
        systemProperty("$schemaGenProp.create-script-source", "$db/create.sql")
        systemProperty("$persistenceProp.sql-load-script-source", "$db/insert.sql")
    }
}
tasks.test {
    useJUnitPlatform {
        includeTags = setOf("integration", "unit")
    }
    val fork = System.getProperty("fork") ?: "1"
    maxParallelForks = fork.toInt()
    systemProperty("db.host", "localhost")
    systemProperty("javax.net.ssl.trustStore", "./src/main/resources/truststore.p12")
    systemProperty("javax.net.ssl.trustStorePassword", "zimmermann")
    systemProperty("junit.platform.output.capture.stdout", true)
    systemProperty("junit.platform.output.capture.stderr", true)
    systemProperty("spring.config.location", "classpath:/application.yml")
    systemProperty("server.ssl.enabled", false)
    systemProperty("server.http2.enabled", false)
    systemProperty("server.ssl.client-auth", "NONE")
    systemProperty("server.tomcat.basedir", "./build/tomcat")
    systemProperty("LOG_PATH", "./build/log")
    systemProperty("APPLICATION_LOGLEVEL", "DEBUG")
    systemProperty("HIBERNATE_LOGLEVEL", "DEBUG")
    systemProperty("spring.datasource.password", "p")
    val persistenceProp = "spring.jpa.properties.jakarta.persistence"
    val schemaGenProp = "$persistenceProp.schema-generation"
    val db = System.getProperty("db")
    if (db == "mysql" || db == "oracle") {
        val dbUrl = when (db) {
            "mysql" -> "jdbc:mysql://localhost/filiale"
            "oracle" -> "jdbc:oracle:thin:@localhost/XEPDB1"
            else -> throw IllegalStateException("Fehler bei der Gradle-Option -Ddb=mysql oder -Ddb=oracle")
        }
        systemProperty("spring.datasource.url", dbUrl)
        systemProperty("$schemaGenProp.drop-script-source", "$db/drop.sql")
        systemProperty("$schemaGenProp.create-script-source", "$db/create.sql")
        systemProperty("$persistenceProp.sql-load-script-source", "$db/insert.sql")
    }
    jvmArgs("--enable-preview")
}
allure {
    version.set(libs.versions.allure.get())
    adapter {
        frameworks {
            junit5 {
                adapterVersion.set(libs.versions.allureJunit.get())
                autoconfigureListeners.set(true)
                enabled.set(true)
            }
        }
        autoconfigure.set(true)
        aspectjWeaver.set(false)
        aspectjVersion.set(libs.versions.aspectjweaver.get())
    }
}
jacoco {
    toolVersion = libs.versions.jacoco.get()
}
tasks.getByName<JacocoReport>("jacocoTestReport") {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    afterEvaluate {
        classDirectories.setFrom(
            files(
                classDirectories.files.map {
                    fileTree(it) { exclude("**/config/**", "**/entity/**") }
                },
            ),
        )
    }
    dependsOn(tasks.test)
}
tasks.getByName<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    violationRules {
        rule {
            limit {
                minimum = BigDecimal("0.7")
            }
        }
    }
}
checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    configFile = file("extras/checkstyle.xml")
    setConfigProperties(
        "configDir" to "$projectDir/extras",
        )
    isIgnoreFailures = false
}
tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
spotbugs {
    toolVersion.set(libs.versions.spotbugs.get())
}
tasks.spotbugsMain {
    reports.create("html") {
        required.set(true)
        outputLocation.set(file("$buildDir/reports/spotbugs.html"))
    }
    excludeFilter.set(file("extras/spotbugs-exclude.xml"))
}
dependencyCheck {
    scanConfigurations = listOf("runtimeClasspath")
    suppressionFile = "$projectDir/extras/owasp.xml"
    data(
        closureOf<org.owasp.dependencycheck.gradle.extension.DataExtension> {
            directory = "C:/Zimmermann/owasp-dependency-check"
            username = "dc"
            password = "p"
        },
    )
    analyzedTypes = listOf("jar")
    analyzers(
        closureOf<org.owasp.dependencycheck.gradle.extension.AnalyzerExtension> {
            assemblyEnabled = false
            autoconfEnabled = false
            bundleAuditEnabled = false
            cmakeEnabled = false
            cocoapodsEnabled = false
            composerEnabled = false
            golangDepEnabled = false
            golangModEnabled = false
            nodeEnabled = false
            nugetconfEnabled = false
            nuspecEnabled = false
            pyDistributionEnabled = false
            pyPackageEnabled = false
            rubygemsEnabled = false
            swiftEnabled = false
            nodeAudit(closureOf<org.owasp.dependencycheck.gradle.extension.NodeAuditExtension> { enabled = true })
            retirejs(closureOf<org.owasp.dependencycheck.gradle.extension.RetireJSExtension> { enabled = true })
        },
    )
    format = org.owasp.dependencycheck.reporting.ReportGenerator.Format.ALL
}
snyk {
    setArguments("--configuration-matching=implementation|runtimeOnly")
    setSeverity("low")
    setApi("40df2078-e1a3-4f28-b913-e2babbe427fd")
}
tasks.javadoc {
    options {
        showFromPackage()
        this as CoreJavadocOptions
        addStringOption("-author")
        addStringOption("Xdoclint:none", "-quiet")
        addBooleanOption("-enable-preview", true)
        addStringOption("-release", "19")
    }
}
tasks.getByName<AsciidoctorTask>("asciidoctor") {
    asciidoctorj {
        setVersion(libs.versions.asciidoctorj.get())
        modules {
            diagram.use()
            diagram.setVersion(libs.versions.asciidoctorjDiagram.get())
        }
    }
    val separator = System.getProperty("file.separator")
    @Suppress("StringLiteralDuplication") setBaseDir(file("extras${separator}doc"))
    setSourceDir(file("extras${separator}doc"))
    logDocuments = true
    inProcess = org.asciidoctor.gradle.base.process.ProcessMode.JAVA_EXEC
    forkOptions {
        @Suppress("StringLiteralDuplication") jvmArgs(
            "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens", "java.base/java.io=ALL-UNNAMED"
        )
    }
    doLast {
        @Suppress("MaxLineLength") println(
            "Das Entwicklerhandbuch ist in $buildDir${separator}docs${separator}asciidoc${separator}entwicklerhandbuch.html", // ktlint-disable max-line-length
        )
    }
}
tasks.getByName<AsciidoctorPdfTask>("asciidoctorPdf") {
    asciidoctorj {
        setVersion(libs.versions.asciidoctorj.get())
        modules {
            diagram.use()
            diagram.setVersion(libs.versions.asciidoctorjDiagram.get())
            pdf.setVersion(libs.versions.asciidoctorjPdf.get())
        }
    }
    val separator = System.getProperty("file.separator")
    setBaseDir(file("extras${separator}doc"))
    setSourceDir(file("extras${separator}doc"))
    attributes(mapOf("pdf-page-size" to "A4"))
    logDocuments = true
    inProcess = org.asciidoctor.gradle.base.process.ProcessMode.JAVA_EXEC
    forkOptions {
        jvmArgs("--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens", "java.base/java.io=ALL-UNNAMED")
    }
    doLast {
        @Suppress("MaxLineLength") println(
            "Das Entwicklerhandbuch ist in $buildDir${separator}docs${separator}asciidocPdf${separator}entwicklerhandbuch.pdf", // ktlint-disable max-line-length
        )
    }
}
licenseReport {
    configurations = arrayOf("runtimeClasspath")
}
tasks.getByName<DependencyUpdatesTask>("dependencyUpdates") {
    checkConstraints = true
}
idea {
    module {
        isDownloadJavadoc = true
        sourceDirs.add(file("generated/"))
        generatedSourceDirs.add(file("generated/"))
    }
}
