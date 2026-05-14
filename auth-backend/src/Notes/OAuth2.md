By default, [Spring Security](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html) uses a strict pre-configured endpoint structure to process the authentication handshake response from Google. [1]
When developing and running your Spring Boot application locally, fill out the fields exactly as follows:
## 1. Authorized Redirect URIs
This field is mandatory. Since your backend handles the login response, paste this exact default URI path into the red box: [1, 2, 3]

http://localhost:8080/login/oauth2/code/google


* http://localhost:8080: Represents your local running Spring Boot application platform server. Change the port number if your server.port configuration property differs.
* /login/oauth2/code/google: The internal endpoint template required by the default Spring OAuth2 client filter layer to receive and process authorization codes. [1, 4]

## 2. Authorized JavaScript Origins
If you are strictly building a monolithic backend web app (using Thymeleaf templates, HTML views, or server-side redirection templates), you can leave this field completely empty.
If you are communicating with a separate Frontend Single Page Application (SPA) like React, Angular, or Vue running locally, add your frontend server address:

* http://localhost:3000 (Typical React default port)
* http://localhost:4200 (Typical Angular default port)

------------------------------
