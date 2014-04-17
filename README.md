Myra Job Scheduling Framework for Java
====

Myra is a framework for job scheduling and management of DAG (Directed Acyclic Graph) or single jobs for java.

Myra is not just a scheduler, it is a combination of a scheduler and dependency chain management framework as it has a strong state transition management capabilities.

## Some features of Myra are:

* It can manage DAG (Directed Acyclic Graph) styled job chains
* Common scheduling definitions mostly covered
* Runtime monitoring and job managment including start, stop, suspend, resum etc.
* Modular architecture enables adding custom job types
* Persistency for failover
* Detection for scyclic dependency conflicts
* Manual start option for job chain definitions
* T < 1 day job chain definitions

### Releases
First stable release is planned to be delivered by the first week of May 2014

### Mail Group

Please join the mail group if you are interested in using or contributing to the development of Myra

[http://groups.google.com/myra-tr](https://groups.google.com/forum/?hl=en#!forum/myra-tr)

#### License

Myra is available under the Apache 2 License.

#### Copyright

Copyright (c) 2008-2014, Likya Bilgi Teknolojileri ve İlet. Hiz. Ltd. Şti. All Rights Reserved.

Visit [www.likyateknoloji.com](http://www.likyateknoloji.com/) for more info.

#### Note
Please get these [jars](http://www.tlos.com.tr/myra/) from our server.

Install to local repo :

mvn install:install-file -Dfile=/downloadedpath/myra-commons-0.0.1.jar  -DgroupId=likyateknoloji -DartifactId=myra-commons -Dversion=0.0.1 -Dpackaging=jar 

mvn install:install-file -Dfile=/downloadedpath/myra-schema-0.0.1.jar  -DgroupId=likyateknoloji -DartifactId=myra-schema -Dversion=0.0.1 -Dpackaging=jar 

mvn install:install-file -Dfile=/downloadedpath/likya-commons-0.0.1.jar  -DgroupId=likyateknoloji -DartifactId=likya-commons -Dversion=0.0.1 -Dpackaging=jar 

Thanks to [mkyong]( http://www.mkyong.com/maven/how-to-include-library-manully-into-maven-local-repository/) for maven install guide
