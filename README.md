# ordinator

Web app for doing buying group orders from Essential Trading

This app facilitates the sharing of whole cases of products between buying group members

## running

In the project directory, open two command shells.

In the first, issue the command:

```
./integration wait
```

This lanches an embedded `dynamodb-local` and then the web-app itself.

In the second, issue the command:

```
lein trampoline
```

This launches the client-side javascript app.
