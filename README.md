# scott.jgroups.logger
[![Build Status](https://travis-ci.org/scotthaleen/jgroups-logger.svg?branch=master)](https://travis-ci.org/scotthaleen/jgroups-logger)

A custom logger for JGroups to bridge JGroups logging to `org.clojure/tools.logging`


```clj
[scott.jgroups.logger "0.1.0"]
```

## Usage

Add this jar to the class path of a JGroups project and set the `jgroups.log_class` system property

`-Djgroups.log_class=scott.jgroups.Logger`

See instructions at the [jgroups manual](http://www.jgroups.org/manual4/index.html#_setting_the_preferred_log_class)

## Test

```
lein test
```

## Compile
```
lein jar
```

## License

Copyright © 2019

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
