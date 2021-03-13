# User Handbook

## This handbook covers the main roadmap on how to use our *breathtaking* URL shortener.


### Endpoints Roadmap
* [Sign Up](#sign-up)
* [Sign In](#sign-in)
* [Create a URL alias](#create-a-url-alias)
* [List user’s shortened links](#list-users-shortened-links)
* [Redirect by shortened URL](#redirect-by-shortened-url)
* [Delete shortened link](#delete-shortened-link)
* [Sign Out](#sign-out)


## Endpoints

### Sign Up
```bash
$ curl --location --request POST 'localhost:8080/users/signup' \
    --header 'Content-Type: application/json' \
    --data-raw '{
        "email": "test@ex.com",
        "password": "Password1"
    }'
```
#### Example output
```text
User was successfully registered.
```

### Sign In
```console
$ curl --location --request POST 'localhost:8080/users/signin' \
    --header 'Content-Type: application/json' \
    --data-raw '{
        "email": "test@ex.com",
        "password": "Password1"
    }'
```
#### Example output
```json
{
    "token": "<token>"
}
```

### Create a URL alias
```bash
$ curl --location --request POST 'localhost:8080/urls/shorten' \
    --header 'Authorization: Bearer <token>' \
    --header 'Content-Type: application/json' \
    --data-raw '{
        "url": "https://google.com",
        "alias": "example"
    }'
```
#### Example output
```json
{
  "shortened_url": "http://localhost:8080/r/example"
}
```

### List user’s shortened links
```bash
$ curl --location --request GET 'localhost:8080/urls/' \
    --header 'Authorization: Bearer <token>'
```
#### Example output
```json
[
    {
        "alias": "example",
        "url": "https://google.com",
        "userId": 1,
        "usages": 0
    }
]
```

### Redirect by shortened URL
```bash
$ curl --location --request GET 'localhost:8080/r/example'
```

### Delete shortened link
```bash
$ curl --location --request DELETE 'localhost:8080/urls/example' \
    --header 'Authorization: Bearer <token>'
```

### Sign Out
```bash
$ curl --location --request GET 'localhost:8080/users/signout' \
    --header 'Authorization: Bearer <token>'
```

#### Example output
```text
Successfully signed out.
```
