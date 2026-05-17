# OAuth2FailureHandler Explanation

## Purpose

`OAuth2FailureHandler` runs when OAuth2 login fails.

For example, if Google login fails because the user denies consent, the OAuth provider rejects the request, or Spring cannot complete authentication, Spring Security calls this handler.

File:

```text
src/main/java/com/example/auth_backend/config/OAuth2FailureHandler.java
```

## 1. Spring Finds This Class

```java
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler
```

`@Component` tells Spring to create this class as a bean.

`implements AuthenticationFailureHandler` tells Spring Security:

```text
This class knows what to do when authentication fails.
```

Then in `SecurityConfig`, it is used here:

```java
.failureHandler(authenticationFailureHandler)
```

So when OAuth2 login fails, Spring calls this handler automatically.

## 2. It Reads The Frontend Failure URL

```java
@Value("${security.app.auth.frontend.failure-redirect}")
private String frontEndFailureUrl;
```

This value comes from your `application-dev.yaml`:

```yaml
security:
  app:
    auth:
      frontend:
        failure-redirect: http://localhost:5173/oauth/failure
```

So inside the Java class:

```java
frontEndFailureUrl = "http://localhost:5173/oauth/failure"
```

## 3. Main Method Runs On Failure

```java
@Override
public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
) throws IOException, ServletException {
```

Spring Security passes three things:

`request`: the incoming HTTP request

`response`: the HTTP response we send back

`exception`: the reason authentication failed

Example exception message:

```text
OAuth2 authorization request failed
```

or:

```text
Access Denied
```

## 4. It Logs The Error

```java
logger.warn("OAuth2 authentication failed: {}", exception.getMessage());
```

This prints the failure reason in the backend logs.

Example log:

```text
OAuth2 authentication failed: Access Denied
```

This is useful for debugging.

## 5. It Gets A Safe Error Message

```java
String errorMessage = exception.getMessage() == null
        ? "OAuth2 authentication failed"
        : exception.getMessage();
```

If Spring gives an error message, it uses that.

If the message is `null`, it uses a fallback:

```text
OAuth2 authentication failed
```

This avoids sending an empty error to the frontend.

## 6. It Redirects To The Frontend

```java
response.sendRedirect(buildFailureRedirectUrl(errorMessage));
```

This sends the browser to your React failure page.

It does not return JSON because OAuth2 login happens in the browser with redirects.

## 7. It Builds The Redirect URL

```java
private String buildFailureRedirectUrl(String errorMessage) {
    String separator = frontEndFailureUrl.contains("?") ? "&" : "?";
    String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
    return frontEndFailureUrl + separator + "error=" + encodedMessage;
}
```

This method adds the error message to the frontend URL.

If the URL has no query params:

```text
http://localhost:5173/oauth/failure
```

it adds:

```text
?error=...
```

Final result:

```text
http://localhost:5173/oauth/failure?error=Access+Denied
```

If the URL already had query params:

```text
http://localhost:5173/oauth/failure?source=google
```

it would add:

```text
&error=...
```

Final result:

```text
http://localhost:5173/oauth/failure?source=google&error=Access+Denied
```

## Simple Example

Suppose the user clicks:

```text
http://localhost:8083/oauth2/authorization/google
```

Then Google login opens.

Now suppose the user cancels login or denies permission.

Spring Security receives the failure and calls:

```java
onAuthenticationFailure(request, response, exception)
```

Assume:

```java
exception.getMessage() = "Access Denied"
```

The handler builds this URL:

```text
http://localhost:5173/oauth/failure?error=Access+Denied
```

Then this line redirects the browser:

```java
response.sendRedirect(...)
```

So the user lands on your React page:

```text
/oauth/failure
```

and your frontend can read the error from the URL query parameter:

```js
const params = new URLSearchParams(window.location.search);
const error = params.get("error");
```

`error` will be:

```text
Access Denied
```

## Flow Summary

```text
User starts OAuth2 login
        ↓
OAuth2 provider or Spring login fails
        ↓
Spring Security calls OAuth2FailureHandler
        ↓
Handler logs the error
        ↓
Handler builds frontend failure URL
        ↓
Browser redirects to React failure page
        ↓
Frontend displays the error message
```
