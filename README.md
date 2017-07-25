# SimpleJSON for Log4J 2.x

Simple JSON for Log4J 2.x, without external dependencies. Open Source Java project under Apache License v2.0

### Current Stable Version is [1.0.0](https://search.maven.org/#search|ga|1|g%3Aorg.javastack%20a%3Alog4j2-simplejson)

---

## DOC

### Supported Log4j2 Versions

| SimpleJSON | Log4j2 |
| :--------- | :----- |
| 1.0.x      | 2.6.x / 2.7.x  |

### What provides this module? 

###### Layout & PatternConverter 

#### SimpleJSONLayout

Appends a series of JSON events as strings serialized as bytes. This is like JSONLayout but without external dependencies and simplified (`compact=true` always).

###### By default, output will be like:

```json
{"layout.version":1,"layout.start":1470563445561,"layout.sequence":1,"timestamp":1470563445639,"thread":"main","threadId":1,"level":"INFO","logger":"org.javastack.log4j.simplejson.sandbox.TestSimpleJson","msg":"Hello world"}
{"layout.version":1,"layout.start":1470563445561,"layout.sequence":2,"timestamp":1470563445639,"thread":"main","threadId":1,"level":"INFO","logger":"org.javastack.log4j.simplejson.sandbox.TestSimpleJson","ndc":["ndc1","ndc2"],"mdc":{"k1":"v1","k2":"v2"},"msg":"Diagnostic context information is wonder"}
{"layout.version":1,"layout.start":1470563445561,"layout.sequence":3,"timestamp":1470563445639,"thread":"main","threadId":1,"level":"INFO","logger":"org.javastack.log4j.simplejson.sandbox.TestSimpleJson","ndc":["ndc1"],"mdc":{"k1":"v1","k2":"v2"},"msg":"log4j2 is wonder"}
{"layout.version":1,"layout.start":1470563445561,"layout.sequence":4,"timestamp":1470563445639,"thread":"main","threadId":1,"level":"INFO","logger":"org.javastack.log4j.simplejson.sandbox.TestSimpleJson","ndc":["ndc1"],"mdc":{"k1":"v1","k2":"v2"},"msg":"Oops!","exception":"java.lang.Exception","cause":"one exception","stacktrace":"java.lang.Exception: one exception\n\tat org.javastack.log4j.simplejson.sandbox.TestSimpleJson.main(TestSimpleJson.java:26) [classes\/:?]\n"}
```

SimpleJSONLayout Parameters

| Parameter Name  | Type           | Default  | Description |
| :-------------- | :------------- | :------- | :---------- |
| charset         | String         | US-ASCII | The character set to use when converting to a byte array. The value must be a valid Charset. |
| eventEol        | boolean        | true     | If true, the appender appends an end-of-line after each record. Use with eventEol=true to get one record per line. |
| complete        | boolean        | false    | If true, the appender includes the JSON header and footer, and comma between records. |
| properties      | boolean        | true     | If true, the appender includes the thread context map in the generated JSON. |
| locationInfo    | boolean        | false    | If true, the appender includes the location information in the generated JSON. Generating [location information](https://logging.apache.org/log4j/2.x/manual/layouts.html#LocationInformation) is an expensive operation and may impact performance. Use with caution. |
| AdditionalField | KeyValuePair[] | none     | Additional fields to include in JSON, these Fields come from [Lookups](https://logging.apache.org/log4j/2.x/manual/lookups.html) |

##### Usage Example of SimpleJSONLayout

###### log4j2.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="warn" name="XMLConfig"
	packages="org.javastack.log4j.simplejson">

	<Appenders>
		<Console name="console">
			<SimpleJSONLayout properties="false">
			    <!-- Additional Fields -->
				<KeyValuePair key="javaVersion" value="java:version" />
			</SimpleJSONLayout>
		</Console>
	</Appenders>
	<Loggers>
		<Root level="info">
			<AppenderRef ref="console" />
		</Root>
	</Loggers>
</Configuration>
```

###### log4j2.properties

```properties
status = warn
name = PropertiesConfig
packages = org.javastack.log4j.simplejson

# Console
appender.console.type = Console
appender.console.name = console
appender.console.layout.type = SimpleJSONLayout
appender.console.layout.properties = false
## Additional Fields
appender.console.layout.additionalfield_1.type = KeyValuePair
appender.console.layout.additionalfield_1.key = javaVersion
appender.console.layout.additionalfield_1.value = java:version

rootLogger.level = info
rootLogger.appenderRef.console.ref = console
```

###### Output will be:

```json
{"layout.version":1,"layout.start":1470564523434,"layout.sequence":1,"timestamp":1470564523512,"thread":"main","threadId":1,"level":"INFO","logger":"com.acme.TestSimpleJson","javaVersion":"Java version 1.7.0_80","msg":"Hello world"}
```

#### SimpleJSONPatternConverter for core PatternLayout

The conversions that are provided with SimpleJSON are: 

| Conversion Pattern  | Description |
| :------------------ | :---------- |
| **json**{pattern}   | Encodes special characters such as '\n' according to JSON-String [RFC-7159](https://tools.ietf.org/html/rfc7159). A typical usage would encode the message  ```%json{%m}``` |

##### Usage Example of PatternConverter (`%json{pattern}`)

###### log4j2.xml

```xml
<Configuration status="warn" name="XMLConfig"
	packages="org.javastack.log4j.simplejson">

	<Appenders>
		<Console name="console">
			<PatternLayout
				pattern='{ "msg":"%json{%m}"%notEmpty{, "exception":"%json{%xEx}"} }%n'
				alwaysWriteExceptions="false" />
		</Console>
	</Appenders>
	<Loggers>
		<Root level="info">
			<AppenderRef ref="console" />
		</Root>
	</Loggers>
</Configuration>
```

###### log4j2.properties

```properties
status = warn
name = PropertiesConfig
packages = org.javastack.log4j.simplejson

# Console
appender.console.type = Console
appender.console.name = console
## Cooked JSON
appender.console.layout.type = PatternLayout
appender.console.layout.pattern = { "msg":"%json{%m}"%notEmpty{, "exception":"%json{%xEx}"} }%n
appender.console.layout.alwaysWriteExceptions = false

rootLogger.level = info
rootLogger.appenderRef.console.ref = console
```

###### Output will be:

```json
{ "msg":"Hello world" }
{ "msg":"Oops!", "exception":"java.lang.Exception: one exception\n\tat org.javastack.log4j.simplejson.sandbox.TestSimpleJson.main(TestSimpleJson.java:26) [classes\/:?]\n" }
```

---

## MAVEN

Add the dependency to your pom.xml:

    <dependency>
        <groupId>org.javastack</groupId>
        <artifactId>log4j2-simplejson</artifactId>
        <version>1.0.0</version>
    </dependency>

---
Inspired in [log4j2-layouts](https://logging.apache.org/log4j/2.x/manual/layouts.html), this code is Java-minimalistic version.
