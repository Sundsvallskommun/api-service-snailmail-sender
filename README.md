# SnailmailSender

## Leverantör
Sundsvalls Kommun

## Beskrivning
SnailmailSender används för att skicka fysiska brev.

## Tekniska detaljer

### Konfiguration

Konfiguration sker i filen `src/main/resources/application.properties` genom att sätta nedanstående properties till önskade värden:

|Property|Beskrivning|
|---|---|
|`integration.email-sender.base-url`|API-URL till EmailSender-tjänsten
|`integration.email-sender.token-uri`|URL för att hämta OAuth2-token för EmailSender-tjänsten
|`integration.email-sender.client-id`|OAuth2-klient-id för EmailSender-tjänsten
|`integration.email-sender.client-secret`|OAuth2-klient-nyckel EmailSender-tjänsten
|`integration.email-sender.sender.address`|E-postadress för avsändare för utgående e-post
|`integration.email-sender.sender.name`|Namn på avsändare för utgående e-post
|`integration.email-sender.sender.replyTo`|Returadress för e-post


### Paketera och starta tjänsten

Paketera tjänsten som en körbar JAR-fil genom:

```
mvn package
```

Starta med:

```
java -jar target/api-service-snailmail-sender-<VERSION>.jar
```

### Bygga och starta tjänsten med Docker

Bygg en Docker-image av tjänsten:

```
mvn spring-boot:build-image
```

Starta en Docker-container:

```
docker run -i --rm -p 8080:8080 evil.sundsvall.se/ms-snailmail-sender:latest
```


Copyright &copy; 2022 Sundsvalls Kommun
