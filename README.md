This API allows to validate the signatures of a posted file. It also supports zip file packages. 

# Build
In order to build the application using maven user needs to set environmental variable:
`SPRING_CONFIG_NAME=eidasProperties`

# Run
### Docker setup and basic run
<!-- TODO: this section needs deep review due to developments in docker -->
In order to run the application user needs to setup his docker. The description will be based on the following environmental properties:
  * DOCKER_CERT_PATH:               path/to/docker/cert/
  * DOCKER_HOST:                    https://192.168.99.100:2376
  * DOCKER_MACHINE_NAME:            default
  * DOCKER_TLS_VERIFY:              1
  * DOCKER_TOOLBOX_INSTALL_PATH:    docker/toolbox/install/path
<!-- to this point -->
Then in docker terminal:
`docker run -p 8080:8080 -t eidascryptomage/eidasverifyservice`

### Tmp folder
It is advised to map a temporary folder on the host machine. It will hold all extracted files (attachments and zip packages) in order to not expand volume unnecessary. To to this you'll need to add -v option:
`docker run -p 8080:8080 -v <pathToYourTmpDir>:/tmp -t eidascryptomage/eidasverifyservice`
For example:
`docker run -p 8080:8080 -v /c/Users/user1/myTmp:/tmp -t eidascryptomage/eidasverifyservice`

### Configuration
#### Properties
In order to change the properties you need to create an eidasProperties.yaml file, with the following content:
~~~~
eidas:
  countrySpecific:
    PL:
      enableTrustedProfileValidation: true
      trustedProfileKeystoreFile: MIIJc(...)rz5VA==
      trustedProfileKeystoreName: itcurzad_1.p12
      trustedProfileKeystorePass: 12345
      trustedProfileUrl: https://int.pz.gov.pl/pz-services/SignatureVerification
  dssValidation:
    defaultConstraints: basicConstraints.xml
    ojKeystoreFile: MIIYpA(...)gQA
    ojKeystoreName: keystore.p12
    ojKeystorePass: eidaspass
    lotlUrl: https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml
    lotlCode: EU
    addDssDetailedReport: true
    addDssDiagnosticReport: true
    trustedCertificateFiles:
      - Q2V(...)S0tLS0tCg==
    trustedCertificateFilesNames:
      - ltc-root-ca.crt
  general:
    tempDirName: tmp
    maxReccurenceNesting: 10
  scheduler:
    cron: 0 0 12 * * ?
spring:
  http:
    multipart:
      maxFileSize: 20MB
      maxRequestSize: 20MB
---
spring:
  profiles: prod
server:
  port: 80
  ssl:
    key-store: classpath:sslKeystore.p12
    key-store-password: eidasSSL
    key-password: eidasKeySSL
    key-store-type: PKCS12
     
~~~~

#### Properties definition
  * **enableTrustedProfileValidation** - Enabling/disabling validation of polish trusted profile
  * **trustedProifleKeystoreFile** - Trusted profile keystore file encoded in base64
  * **trustedProfileKeystoreName** - Name of the trusted profile keystore file
  * **trustedProfileKeystorePass** -  Password to the trusted profile keystore 
  * **trustedProfileUrl** - Url for trusted profile validation should be set to: https://pz.gov.pl/pz-services/SignatureVerification
  * **defaultConstraints** - Default constraints used to validate files with DSS library, there is a basicConstraints.xml and dssDefaultConstraints.xml available
  * **ojKeystoreFile** - Keystore with OJ certificates encoded in base 64
  * **ojKeystoreName** - Keystore filename which should hold OJ certificates (more in OJ certificates)
  * **ojKeystorePass** - Password to keystore with OJ certificates
  * **lotlUrl** - Url for list of the trusted lists
  * **lotlCode** - List of the trusted lists code
  * **addDssDetailedReport** - If dss detailed report should be added to the response
  * **addDssDiagnosticReport** - If dss diagnostic tree should be added to the response
  * **trustedCertificateFiles** -  A list of trusted certificate files that you want to set as trusted in dss validation process. Encoded in base64
  * **trustedCertificateFilesNames** - Trusted certificate files names coresponding to the trusted certificate files - must have equal number of variables - otherwise none of these certificates will be added as trusted
  * **tempDirName** - Name of the temporary folder - it's best to not change this variable as the name of the folder to mount in docker would change
  * **maxReccurenceNesting** - How far the service should extract the zip files and attachments
  * **scheduler.cron** - Cron scheduler statement that defines how often application will refresh it's online certificate sources
  * **spring.http.multipart.maxFileSize** - Maximum file size sent to the service
  * **spring.http.multipart.maxRequestSize** - Maximum request size sent to the service
  * **server.port** - Production port
  * **server.ssl.key-store** - Production ssl keystore name
  * **server.ssl.key-store-password** - Production ssl keystore password
  * **server.ssl.key-password** - Production ssl key password
  * **server.ssl.key-store-type** - Production keystore type

#### Changing variables
Example above shows every possible variable default value, but you don't need to specify all of them. If you want to change only one variable, you can provide a file with only this variable set. In order to apply this properties you need to run the application in the following manner:
`docker run -p 8080:8080 -v <path to your properties>:/eidasProperties.yaml -t eidascryptomage/eidasverifyservice`
For example:
`docker run -p 8080:8080 -v /c/files/eidasProperties.yaml:/eidasProperties.yaml -t eidascryptomage/eidasverifyservice`

You can provide multiple certificates if you define them like shown below:
~~~~
eidas:
  dssValidation:
    trustedCertificateFiles:
      - Q2V(...)S0tLS0tCg==
      - gICB(...)gICAgICA==
    trustedCertificateFilesNames:
      - cert1name.crt
      - cert2name.crt
~~~~

Mind that you can join -v variables and you can also map a temporary volume like this:
`docker run -p 8080:8080 -v <path to your tmp folder>:/tmp -v <path to your properties>:/eidasProperties.yaml -t eidascryptomage/eidasverifyservice`
For example:
`docker run -p 8080:8080 -v /c/eidas/tmp:/tmp -v /c/eidas/eidasProperties.yaml:/eidasProperties.yaml -t eidascryptomage/eidasverifyservice`

This will allow the application to access your properties file. You can then change its contents - setting any variable you want - and apply the changes by `docker restart <containerName>` command.

# OJ certificates
In order to get the LOTL from https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml certificate keystore (eidas.keystore.file) has to have allowed certificates (extracted from the OJ). The latest certificates are extracted from http://eur-lex.europa.eu/legal-content/PL/TXT/?uri=CELEX%3A52016XC0628%2801%29. However they can expire and have to be extracted again, if application will validate signatures with certificates stored in the official LOTL. 

# Testing
After running the application like described in the previous chapter, you can use the service.
On a '192.168.99.100:8080/swagger-ui.html/' endpoint there is a handy ui generated with springfox swagger plugin. You can check every endpoint made available in the service and test it in your browser. This ui serves as a documentation for the service.
Request response will appear at the bottom in form of a json.
On a '192.168.99.100:8080/v2/api-docs' endpoint there is a json representation of swagger generated ui.

# Automatic deployment
This repository is configured with continous deployment to [http://35.161.114.220:8080/](http://35.161.114.220:8080/).