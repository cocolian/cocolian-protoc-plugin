
cocolian-protoc-plugin
=======================

参考 https://github.com/os72/protoc-jar-maven-plugin 这个项目。 

cocolian-protoc-plugin 是一个maven插件，用于解决使用protocol buffer (protobuf)的大型软件项目中，对protobuf文件的管理和编译的问题。 
在大型软件项目中使用protobuf时，一般有两种处理方式：
1. 集中管理protobuf文件，这会导致这个项目非常庞大，而且不同项目由于对protobuf的使用而耦合在一起。 
2. 分项目管理protobuf文件，这带来的问题是，如果一个项目需要引用其他项目的protobuf文件，需要将原文件复制过来，这不利于跟踪文件版本的变化，也不便于开发和管理。 

cocolian-protoc-plugin主要是面向第二种protobuf文件管理而支持的：
1. 每个项目可以定义自己的protobuf文件，并打包到jar中。 
2. 如果引用其他项目的protobuf，可以直接按照jar中打包的路径来使用，不需要复制文件。 
3. 仅编译产生本项目protobuf文件的java代码。 
4. 无需安装protobuf，自动识别和下载protoc文件。当然，也可以配置使用自己的protoc命令。 
5. 编译产生的java代码放在main/gen目录下，和开发的代码分离，便于管理。这个目录在编译后会自动被添加到编译路径中。 

### 简单示例

生成java代码到`target/generated-sources`目录, 添加代码到项目中，使用默认的protoc命令， proto文件存放在 `src/main/protobuf` 目录下：
```xml
<plugin>
	<groupId>org.jigsaw.payment</groupId>
	<artifactId>jigsaw-protoc-plugin</artifactId>
	<version>1.0.0</version>
	<executions>
		<execution>
			<phase>generate-sources</phase>
			<goals>
				<goal>run</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

### 进阶1 

引入`google.protobuf` 标准数据类型, 增加额外的依赖：

```xml
<plugin>
	<groupId>org.jigsaw.payment</groupId>
	<artifactId>jigsaw-protoc-plugin</artifactId>
	<version>1.0.0</version>
	<executions>
		<execution>
			<phase>generate-sources</phase>
			<goals>
				<goal>run</goal>
			</goals>
			<configuration>
				<protocVersion>3.5.0</protocVersion>
				<includeStdTypes>true</includeStdTypes>
				<includeDirectories>
					<include>src/main/more_proto_imports</include>
				</includeDirectories>
				<inputDirectories>
					<include>src/main/protobuf</include>
				</inputDirectories>
			</configuration>
		</execution>
	</executions>
</plugin>
```

### 进阶2

从maven库中下载protoc文件, 支持多种输出格式： 

```xml
<plugin>
	<groupId>org.jigsaw.payment</groupId>
	<artifactId>jigsaw-protoc-plugin</artifactId>
	<version>3.5.0</version>
	<executions>
		<execution>
			<phase>generate-sources</phase>
			<goals>
				<goal>run</goal>
			</goals>
			<configuration>
				<protocArtifact>com.google.protobuf:protoc:3.0.0</protocArtifact>
				<inputDirectories>
					<include>src/main/resources</include>
				</inputDirectories>
				<outputTargets>
					<outputTarget>
						<type>java</type>
					</outputTarget>
					<outputTarget>
						<type>grpc-java</type>
						<pluginArtifact>io.grpc:protoc-gen-grpc-java:1.0.1</pluginArtifact>
					</outputTarget>
				</outputTargets>
			</configuration>
		</execution>
	</executions>
</plugin>
```

### 进阶3

仅编译测试文件， 输出到多个目的目录， 不修改项目内容:
```xml
<plugin>
	<groupId>org.jigsaw.payment</groupId>
	<artifactId>jigsaw-protoc-plugin</artifactId>
	<version>3.5.0</version>
	<executions>
		<execution>
			<phase>generate-test-sources</phase>
			<goals>
				<goal>run</goal>
			</goals>
			<configuration>
				<protocVersion>2.4.1</protocVersion>
				<inputDirectories>
					<include>src/test/resources</include>
				</inputDirectories>
				<outputTargets>
					<outputTarget>
						<type>java</type>
						<addSources>none</addSources>
						<outputDirectory>src/test/java</outputDirectory>
					</outputTarget>
					<outputTarget>
						<type>descriptor</type>
						<addSources>none</addSources>
						<outputDirectory>src/test/resources</outputDirectory>
					</outputTarget>
				</outputTargets>
			</configuration>
		</execution>
	</executions>
</plugin>
```

### 进阶4

使用protoc 2.4.1来编译：

```xml
<plugin>
	<groupId>com.github.os72</groupId>
	<artifactId>protoc-jar-maven-plugin</artifactId>
	<version>3.5.0</version>
	<executions>
		<execution>
			<phase>generate-sources</phase>
			<goals>
				<goal>run</goal>
			</goals>
			<configuration>
				<protocVersion>2.4.1</protocVersion>
				<type>java-shaded</type>
				<addSources>none</addSources>
				<outputDirectory>src/main/java</outputDirectory>
				<inputDirectories>
					<include>src/main/protobuf</include>
				</inputDirectories>
			</configuration>
		</execution>
	</executions>
</plugin>
```

#### Credits

在 [protoc-jar-maven-plugin](https://github.com/os72/protoc-jar-maven-plugin) 基础上开发的。 
