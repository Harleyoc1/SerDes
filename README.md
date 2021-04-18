# SerDes
A library for serialisation and deserialisation to and from an SQL table and a Java object. Built on `Java 16`.

Due to this being in early stages of development, semantic versioning conventions are currently **not** strictly followed. There is also not currently any documentation, though most methods and classes have Javadoc (I am slowly adding Javadoc to all methods and classes, and fields where necessary).

## Gradle Setup
*Don't reinvent the wheel, add it to your build.gradle!*

To add it to your `build.gradle`, first add the following code to the `repositories` section to load my maven repository:

```groovy
maven {
    name 'Harley O\'Connor Maven'
    url 'http://harleyoconnor.com/maven/'
    allowInsecureProtocol = true
}
```

Next, add the following to your `dependencies` section to load java utilities:

```groovy
implementation group: 'com.harleyoconnor.serdes', name: 'SerDes', version: '0.0.5'
```