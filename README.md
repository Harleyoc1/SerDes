# SerDes ![](https://img.shields.io/badge/Java-16-green) [![GitHub](https://img.shields.io/github/license/Harleyoc1/SerDes)](./LICENSE) ![](https://img.shields.io/github/workflow/status/Harleyoc1/SerDes/Java%20CI%20with%20Gradle) [![](https://img.shields.io/github/v/tag/Harleyoc1/SerDes)](https://github.com/Harleyoc1/SerDes/releases)
A library for serialisation and deserialisation to and from an SQL table and a Java object. Built on `Java 16`.

Due to this being in early stages of development, semantic versioning conventions are currently **not** strictly followed. There is also not currently any documentation, though most methods and classes have Javadoc (I am slowly adding Javadoc to all methods and classes, and fields where necessary).

## Gradle Setup
*Don't reinvent the wheel, add it to your build.gradle!*

To add it to your `build.gradle`, first add the following code to the `repositories` section to load my maven repository:

`build.gradle` - For Groovy build scripts.
```groovy
maven {
    name 'Harley O\'Connor Maven'
    allowInsecureProtocol = true
    url 'http://harleyoconnor.com/maven/'
}
```

`build.gradle.kts` - For Kotlin build scripts.
```kotlin
maven("http://harleyoconnor.com/maven/") {
    this.isAllowInsecureProtocol = true
}
```

Next, add the following to your `dependencies` section to load java utilities:

`build.gradle` - For Groovy build scripts.
```groovy
implementation group: 'com.harleyoconnor.serdes', name: 'SerDes', version: '0.0.6'
```

`build.gradle.kts` - For Kotlin build scripts.
```kotlin
implementation(group = "com.harleyoconnor.serdes", name = "SerDes", version = "0.0.6")
```
