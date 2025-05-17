# Files Manager

Application de gestion de fichiers développée avec Spring Boot et architecture hexagonale.

## Fonctionnalités

- Upload de fichiers
- Création de répertoires
- Navigation dans l'arborescence des fichiers
- Suppression de fichiers et répertoires

## Prérequis

- Java 17 ou supérieur
- Maven 3.6 ou supérieur

## Installation

1. Cloner le repository :
```bash
git clone [URL_DU_REPO]
cd filesmanager
```

2. Compiler le projet :
```bash
mvn clean install
```

3. Lancer l'application :
```bash
mvn spring-boot:run
```

L'application sera accessible à l'adresse : http://localhost:8080

## Documentation API

La documentation Swagger de l'API est disponible aux URLs suivantes :

- Interface Swagger UI : http://localhost:8080/swagger-ui/index.html
- Documentation OpenAPI (JSON) : http://localhost:8080/v3/api-docs
- Documentation OpenAPI (YAML) : http://localhost:8080/v3/api-docs.yaml

## Utilisation

### Création de répertoires

Pour créer un répertoire ou une structure de répertoires, utilisez le caractère '_' comme séparateur :

```
POST /api/files/directory/2025_Janvier_Factures
```

### Upload de fichiers

Pour uploader un fichier dans un répertoire spécifique :

1. Utilisez l'endpoint `/api/files/upload`
2. Paramètres :
   - `file` : Le fichier à uploader
   - `targetPath` : Le chemin cible (optionnel, utilisez '_' comme séparateur)

Exemple : Pour uploader dans `2025/Janvier/Factures` :
```
POST /api/files/upload
targetPath: 2025_Janvier_Factures
```

### Stockage des fichiers

Par défaut, les fichiers sont stockés dans :
- Windows : `%USERPROFILE%\AppData\Local\FilesManager`
- Linux/Mac : `~/.filesmanager`

Ce comportement peut être modifié en configurant la propriété `storage.location` dans `application.properties`.

## Architecture

Le projet suit une architecture hexagonale (ports & adapters) avec trois modules principaux :

- `app` : Controllers REST et configuration de l'application
- `domain` : Logique métier et ports (interfaces)
- `infra` : Adapteurs et implémentation technique

## Configuration

Les principales configurations se trouvent dans `src/main/resources/application.properties` :

```properties
# Taille maximale des fichiers
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# Répertoire de stockage (optionnel)
storage.location=
```

## Licence

[Boris Sodoloufo] 