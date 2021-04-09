# SerDes
An API for serialisation and deserialisation to and from an SQL table and a Java object. Currently, it is in early stages of development and testing, and does not have any documentation as of yet.

Note that this is built on and expects to run on `Java 16` (and by hence you will also need `Gradle 7`)!

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
implementation group: 'com.harleyoconnor.serdes', name: 'SerDes', version: '0.0.1'
```