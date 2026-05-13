# Quad assignment - trivia application

This is an implementation of the onboarding assignment for Quad, as specified [here](https://www.quad.team/assignment)

## Running

### TL;DR

```sh
pnpm go
```

Then point your browser to [http://localhost:4200](http://localhost:4200) (after both backend and frontend applications
have finished starting up.)

**note:** this build is not platform-independent. It has been tested on MacOS. It might work on Linux, but for
Windows, some of the commands in the various package.json files may need to be adjusted.

### Build Requirements

The required tooling to run this project are:

* JDK 25
* Maven 3.* (A maven wrapper is provided)
* NodeJS 24.*
* [pnpm](https://pnpm.io) 11.1.0

The application is split into two modules: backend and frontend. For convenience, a root [package.json](package.json) is
provided that allows building and running the application

```sh
pnpm install
```

This will (re)build both the frontend and backend modules.

```sh
pnpm start
```

This will start the backend and frontend modules.

## Functional Requirements

The client requires a web application that meets the following criteria:

* It has a graphical UI that allows users to answer trivia questions
* The application should use the [Open Trivia Database](https://opentdb.com) as a source for questions and answers
* The answers to questions should not be exposed in the response containing thet question.
  Otherwise, a clever user might look up the answer before answering the question

### Additional requirements

The assignment leaves some room for various implementation details. Rather than consulting with the (fictitious) client,
we will pose some additional requirements, constraining those details further, along with an explanation of said
requirement.

* The user is presented with a single question at a time.

  This keeps the UI simple, and allows for a 1-to-1 'mapping' of backend request for a question to an upstream request.

* The user is not presented with the same question twice, unless the questions for the given filter have been depleted
    * In this case, the user will be informed of this, before resetting it's session.
    * Optionally, we can give the user the option to change the filter without resetting the session.

      This way, we're transparent to the user about the limited amount of questions.

## Design choices

### Session

In order to meet the criteria of not presenting the user with the same question twice, we'll need to leverage
the session token feature of the OTDB. This means requesting a session token and storing that somewhere.

Since it is highly preferable to keep the BE stateless, the logical thing to do is store the session token in the FE.
We could pass the token explicitly between BE and FE through the request and response bodies, but that convolutes the
message body and distracts from its functional purpose. A cleaner way is to use a cookie to store the token. That
way, after having it set by the BE through a response header, the FE automatically sends it along with each subsequent
request.

This approach is convenient whenever a client handles cookies properly. Seeing as we're building a modern web client
that runs in a browser, that aspect is covered.

### Rate limiting

The upstream service is ratelimited to 5 requests per second. Instead of implementing some sort of throttling mechanism
or buffering questions so we can fall back to previous stored questions, we transparently communicate this to the user.

The UX is such that the user is informed and can retry after a short delay.

### API

The API of our application is fully documented using OpenAPI 3.1, which is then used to generate both server and client
code. See [api.yml](backend/src/main/resources/api.yml) in the backend module for the spec.

The backend uses the maven
plugin [openapi-generator-maven-plugin](https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-maven-plugin)
to generate the client code., whereas the frontend uses the npm
package [openapi-generator-cli](https://www.npmjs.com/package/openapi-generator-cli).

### Open Trivia DB API response code mapping

| `$.response_code` | HTTP status code | Meaning                                                                                                                                                      |
|------------------:|-----------------:|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
|                 0 |              200 | **Success** Returned results succesfully                                                                                                                     |
|                 1 |              200 | **No Results** Could not return results. The API doesn't have enough questions for your query. (Ex. Asking for 50 Questions in a Category that only has 20.) |
|                 2 |              200 | **Invalid Parameter** Contains an invalid parameter. Arguments passed in aren't valid. (Ex. Amount = Five)                                                   |
|                 3 |              200 | **Token Not Found** Session Token does not exist                                                                                                             |
|                 4 |              200 | **Token Empty** Session Token has returned all possible questions for the specified query. Resetting the Token is necessary.                                 |
|                 5 |              429 | **Rate Limit** Rate Limit Too many requests have occurred. Each IP can only access the API once every 5 seconds.                                             |




