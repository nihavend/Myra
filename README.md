Myra Job Scheduling Framework for Java
====

Myra is a job scheduler and manager of DAG or single jobs for java.

Just to say a scheduler is not enough to describe Myra, as it has strong state management capability, we can define Myra as comibination of a scheduler and dependency chain management framework.

## Some features are:

* Possible to manage DAG styled job chains
* Common scheduling definitions mostly covered
* Runtime monitoring and job managment including start, stop, suspend, resum etc.
* With its modular architecture, it is possible to add custom defined jobs
* Persistency for failover
* Detection for scyclic dependency conflicts
* Manual job chain definitions
* T < 1 day job chain definitions

### Releases
First stable release is planned to be delivered through the first week of April.

### Mail Group

Please join the mail group if you are interested in using or developing Myra.

[http://groups.google.com/myra-tr](https://groups.google.com/forum/?hl=en#!forum/myra-tr)

#### License

Myra is available under the Apache 2 License.

#### Copyright

Copyright (c) 2008-2013, Likya Bilgi Teknolojileri ve İlet. Hiz. Ltd. Şti. All Rights Reserved.

Visit [www.likyateknoloji.com](http://www.likyateknoloji.com/) for more info.

#### Note
Please get these [jars](http://www.tlos.com.tr/myra/) from our server.
Install to local repo :

mvn install:install-file -Dfile=/downloadedpath/myra-commons-0.0.1.jar  -DgroupId=likyateknoloji -DartifactId=myra-commons -Dversion=0.0.1 -Dpackaging=jar 

mvn install:install-file -Dfile=/downloadedpath/myra-schema-0.0.1.jar  -DgroupId=likyateknoloji -DartifactId=myra-schema -Dversion=0.0.1 -Dpackaging=jar 

mvn install:install-file -Dfile=/downloadedpath/likya-commons-commons-0.0.1.jar  -DgroupId=likyateknoloji -DartifactId=likya-commons-commons -Dversion=0.0.1 -Dpackaging=jar 
