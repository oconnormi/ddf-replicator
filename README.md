# DDF Replicator
Create multiple [ddf](http://codice.org/ddf/) configurations from a single export

Currently this is intended to be used alongside the ddf platform:configuration:export utility. The utility allows for cloning these exported configurations and modifying them to create multiple similar systems.

# Building
This project is built using gradle. To build run the following

`gradle assemble`

copy the resulting zip from `build/distributions/`

# Usage
`ddf-replicator /path/to/exported/configs`

```
usage: ddf-replicator -[cho] [src]
 -c,--config-path <config>   Path to config.yaml, defaults to working
                             directory
 -h,--help                   Show usage information
 -o,--output-dir <output>    Path to output directory, defaults to
                             "${PWD}/out"
```

# Configuration
Configuration is specified through a `config.yaml` file.

The config file allows for specifying multiple resulting ddf nodes.
Each node supports the following parameters

## applications
Specify applications for initial start
```yaml
...
  applications:
    # Local Application
    some.app.name=

    # Application installed into a local maven repository:
    opendj-embedded=mvn:org.codice.opendj.embedded/opendj-embedded-app/1.0.1-SNAPSHOT/xml/features

    # Application located on the file system:
    opendj-embedded=file:/location/to/opendj-embedded-app-1.0.1-SNAPSHOT.kar
```

## system
Set DDF System Properties
```yaml
...
  system:
    org.codice.ddf.system.hostname: localhost
    org.codice.ddf.system.siteName: ddf.local
```

## configs
add/edit arbitrary config files
```yaml
...
  configs:
    etc/someFile.config:
      someProperty: someValue
    etc/
```

## includes
Include external files
```yaml
...
  includes:
    - src: path/to/keystore.jks
      dest: etc/keystores/serverKeystore.jks
    - src: path/to/truststore.jks
      dest: etc/keystores/serverTruststore.jks
```

# Example Config
```yaml
node1:
  system:
    sysprop1: val1
    sysprop2: val2
  configs:
    etc/org.ddf.some.file1:
      prop1: val1
      prop2: val2
    etc/org.codice.ddf.ui.searchui.filter.RedirectServlet.config:
      defaultUri: "foo/bar"
    anotherFile:
      someprop: aVal
  applications:
    security-services-app: ""
    foo-app: ""
  includes:
    - src: path/to/keystore.jks
      dest: etc/keystores/serverKeystore.jks
    - src: path/to/truststore.jks
      dest: etc/keystores/serverTruststore.jks
node2:
  configs:
    fooFile:
      barProp: barVal
```
