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

## Status
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-snailmail-sender&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-snailmail-sender)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-snailmail-sender&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-snailmail-sender)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-snailmail-sender&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-snailmail-sender)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-snailmail-sender&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-snailmail-sender)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-snailmail-sender&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-snailmail-sender)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-snailmail-sender&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-snailmail-sender)

## 
Copyright (c) 2021 Sundsvalls kommun
