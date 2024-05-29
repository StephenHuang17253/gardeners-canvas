# SENG302 The Gardener's Grove

Basic project using ```Gradle```, ```Spring Boot```, ```Thymeleaf```, and ```GitLab CI```.

Gardener's Grove is a one-stop-shop app for avid gardeners of all expertise levels. Users will be able to record and identify different fruits and vegetables. They will also be able to manage their gardens with weather reports and history of evolution. Interactions between users will involve sharing tips and tricks and discussing various gardening topics.

#### Note: Currently, reset password emails from our application are being blocked by @uclive.ac.nz emails. This is likely due to them being filtered as spam. The emails are accepted by @gmail.com emails.

## User accounts for testing the application

This project includes:
- user registration
- user login
- profile view
- profile edit
- add profile picture
- verfiy email*
- update password*
- create garden
- garden view
- garden edit
- create/view plant
- plant edit
- add plant picture
- weather monitoring*
- garden location*
- reset password*
- managing friends (adding friends, pending, accepted, and declined invites)
- publicise gardens* 
- browsing public gardens

For more information, check the user manual in the wiki.
_*Thoose features require environment variables._

#### User accounts

| Account Type | Email | Password | Gmail Password | Use: |
|--------------|-------|----------|----------------|------|
| Default User | gardenersgrovetest@gmail.com | Password1! | Password10! | This account is initialised and pre-verified on startup, allowing developers to skip verification steps, to more quickly test features during development. |
| Test User | gardenersgrovetest1@gmail.com | Password10! | Password10! | This account is used when you want/need to go through the account creation steps. It is not created on app startup so the email is not in use. |
| Test User | gardenersgrovetest2@gmail.com | Password10! | TestPassword10! | This account is used when you want/need to go through the account creation steps. It is not created on app startup so the email is not in use. |

## How to run
### 1 - Running the project with full functionality
#### Use the deployed link below

[Deployed Production Server](https://csse-seng302-team500.canterbury.ac.nz/prod/)

#### From the project on IDE...

##### Set the following environment variables within your IDE to run application with full functionality:

These must be set for Gradle, Spring Boot, and Cucumber Java  configurations.

BASE_URL=<your_base_url>

EMAIL_PASSWORD=<your_email>

EMAIL_USERNAME=<your_app_password>

LOCATIONIQ_API_KEY=<your_locationiq_api_key>

AZURE_ENDPOINT=<your_azure_endpoint>

AZURE_MODERATOR_KEY=<your_azure_key>

For running locally on port 8080, <your_base_url> will be http://localhost:8080.

Values for <your_email> and <your_app_password> are located on the repository wiki.

To get <your_locationiq_api_key> you can visit [this link](https://my.locationiq.com/register) and get a key for free (credit card not required).

To get <your_azure_endpoint> and <your_azure_key> you can visit [this link] https://azure.microsoft.com/en-us/products/ai-services/ai-content-safety. There is a cost associated unless you are a student, in which case there is a free plan which gives $100 USD credit.

The credentials for the deployed version be found on the repository within the environment variables. 

##### Then Run (on Intellij)

Right-click on GardenersGroveApplication.java and click Run 'GardenersGroveApp...' from the dropdown menu.

By default, the application will run on local port 8080 [http://localhost:8080](http://localhost:8080)


#### From a terminal (using a jar)...

On your Terminal (Powershell on Windows)

Go to the directory you want to run from: 
```
cd <path_to_directory>
```
Clone the repository:
```
git clone https://eng-git.canterbury.ac.nz/seng302-2024/team-500.git
```
Go to project root:
```
cd team-500
```
Checkout main branch because dev is our default:
```
git checkout main
```
Create jar:
```
./gradlew bootJar
```
Go to jar directory:
```
cd build/libs
```
Run the jar on [http://localhost:8080](http://localhost:8080) with the following command. 
- Note: Fill in <your_locationiq_api_key>, <your_app_password>, and <your_email> with the details you got earlier (instructions on how to receive these values are above). 
- You could copy and paste the command into Notepad or any text editor, and then replace the placeholders.
- e.g. spring.mail.username=gardenersgroveinfo@gmail.com


Steps including setting environment variables (Full functionality)
```
java -jar gardeners-grove-0.0.1-SNAPSHOT.jar \
--server.port=8080 \
--spring.application.name=gardeners-grove \
--locationiq.access.token=<your_locationiq_api_key>\
--spring.mail.username=<your_email> \
--spring.mail.password=<your_app_password> \
--spring.base.url=http://localhost:8080 \
--azure.service.endpoint=<your_azure_endpoint> \
--azure.moderator.token=<your_azure_key>

```

Steps without setting environment variables (Partial functionality)
```
java -jar gardeners-grove-0.0.1-SNAPSHOT.jar \
--server.port=8080 \
--spring.application.name=gardeners-grove \

```


### 2 - Running the project with partial functionality

If environment variables are not set, the app will still build and run but note that some parts of the application will not work.

#### From the root directory of the project on the terminal ...

On Linux:
```
./gradlew bootRun
```

On Windows:
```
gradlew bootRun
```


### 3 -  How to run tests with full functionality
#### From the project open on IDE ...

##### To run unit tests:

Right-click on the unit folder and click on Run 'Tests in ...' from the dropdown menu.
If an additional dropdown appears, click on test.

##### To run integration tests:

Right-click on the integration folder and click on Run 'Tests in ...' from the dropdown menu.
If an additional dropdown appears, click on integration.

##### To run cucumber tests:

Right-click on the cucumber folder and click on Run 'Tests in ...' from the dropdown menu.
If an additional dropdown appears, click on cucumber.

#### From the root directory of the project on the terminal ...

On Linux:
```
./gradlew test
```

On Windows:
```
gradlew test
```

## Contributors

- Lachlan Stewart
- Emma Davis
- Aditi Sharma
- Finlay Widdowson
- Tyler Fauchelle
- Dominic Gorny
- Stephen Huang
- Luke Edwards
- SENG302 teaching team


## References

- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring JPA Docs](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Thymeleaf Docs](https://www.thymeleaf.org/documentation.html)
- [Learn resources](https://learn.canterbury.ac.nz/course/view.php?id=17797&section=8)
- [LocationIQ API Docs](https://docs.locationiq.com/reference/reverse-api)
- [Spring Boot Starter Mail Starter Guide](https://www.baeldung.com/spring-email)
- [Weather API (Open-Meteo) Docs](https://open-meteo.com/en/docs)
- [Profanity API (Azure AI Content Safety) Docs](https://learn.microsoft.com/en-us/azure/ai-services/content-safety/overview)

## Licences

#### API Licences
- [Spring Boot Starter Mail](https://eng-git.canterbury.ac.nz/seng302-2024/team-500/-/wikis/Spring%20Boot%20Starter%20Mail%20Licence)
- [LocationIQ](https://eng-git.canterbury.ac.nz/seng302-2024/team-500/-/wikis/LocationIQ%20Licensing%20Terms)
- [Open Metro](https://eng-git.canterbury.ac.nz/seng302-2024/team-500/-/wikis/Open-Meteo%20Licensing%20Terms)
- [Microsoft Azure](https://eng-git.canterbury.ac.nz/seng302-2024/team-500/-/wikis/Microsoft-Azure-for-Students-Subscription-and-MOSA)
