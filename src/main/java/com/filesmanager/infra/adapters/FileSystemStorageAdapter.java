package com.filesmanager.infra.adapters;

import com.filesmanager.domain.ports.FileStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FileSystemStorageAdapter implements FileStoragePort {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageAdapter.class);
    private final Path rootLocation;

    public FileSystemStorageAdapter(@Value("${storage.location:}") String configuredLocation) {
        this.rootLocation = initializeStorageLocation(configuredLocation);
    }

    private Path initializeStorageLocation(String configuredLocation) {
        try {
            // Si un chemin est configuré explicitement, essayer de l'utiliser d'abord
            if (configuredLocation != null && !configuredLocation.trim().isEmpty()) {
                Path configuredPath = Paths.get(configuredLocation);
                if (isLocationUsable(configuredPath)) {
                    logger.info("Utilisation du répertoire de stockage configuré : {}", configuredPath);
                    Files.createDirectories(configuredPath);
                    return configuredPath;
                }
                logger.warn("Le répertoire configuré {} n'est pas accessible", configuredLocation);
            }

            // Utiliser le répertoire home de l'utilisateur
            String userHome = System.getProperty("user.home");
            Path appDataPath;
            
            // Création du chemin selon le système d'exploitation
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                // Sous Windows, utiliser AppData/Local
                appDataPath = Paths.get(userHome, "AppData", "Local", "FilesManager");
            } else {
                // Sous Linux/Mac, utiliser ~/.filesmanager
                appDataPath = Paths.get(userHome, ".filesmanager");
            }

            if (isLocationUsable(appDataPath)) {
                logger.info("Utilisation du répertoire utilisateur : {}", appDataPath);
                Files.createDirectories(appDataPath);
                return appDataPath;
            }

            // En dernier recours, utiliser le répertoire temporaire
            Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), "filesmanager");
            logger.warn("Utilisation du répertoire temporaire comme fallback : {}", tempPath);
            Files.createDirectories(tempPath);
            return tempPath;

        } catch (IOException e) {
            String errorMsg = "Impossible d'initialiser le stockage. Erreur : " + e.getMessage();
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    private boolean isLocationUsable(Path path) {
        try {
            // Si le chemin n'existe pas, vérifier le droit de création dans le parent
            if (!Files.exists(path)) {
                Path parent = path.getParent();
                if (parent == null) {
                    return false;
                }
                if (!Files.exists(parent)) {
                    return false;
                }
                return Files.isWritable(parent);
            }

            // Si le chemin existe, vérifier tous les droits nécessaires
            File file = path.toFile();
            return file.canRead() && file.canWrite() && file.canExecute();

        } catch (SecurityException e) {
            logger.warn("Erreur de sécurité lors de la vérification des droits sur {} : {}", path, e.getMessage());
            return false;
        }
    }

    @Override
    public void createDirectory(String directoryPath) {
        try {
            Path newDir = this.rootLocation.resolve(directoryPath).normalize().toAbsolutePath();
            if (!newDir.startsWith(this.rootLocation.toAbsolutePath())) {
                throw new RuntimeException("Cannot create directory outside current directory.");
            }
            Files.createDirectories(newDir);
            logger.info("Répertoire créé avec succès : {}", directoryPath);
        } catch (IOException e) {
            String errorMsg = "Échec de la création du répertoire " + directoryPath;
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    @Override
    public void storeFile(String filename, byte[] content) {
        try {
            Path destinationFile = this.rootLocation.resolve(Paths.get(filename)).normalize().toAbsolutePath();
            if (!destinationFile.getParent().startsWith(this.rootLocation.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }
            Files.write(destinationFile, content);
            logger.info("Fichier stocké avec succès : {}", filename);
        } catch (IOException e) {
            String errorMsg = "Échec du stockage du fichier " + filename;
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    @Override
    public List<Path> listFiles() {
        try {
            return Files.walk(this.rootLocation)
                    .filter(path -> !path.equals(this.rootLocation))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            String errorMsg = "Échec de la lecture des fichiers stockés";
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    @Override
    public void deleteFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Files.deleteIfExists(file);
            logger.info("Fichier supprimé avec succès : {}", filename);
        } catch (IOException e) {
            String errorMsg = "Échec de la suppression du fichier " + filename;
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    @Override
    public void deleteDirectory(String directoryPath) {
        try {
            Path directory = rootLocation.resolve(directoryPath);
            FileSystemUtils.deleteRecursively(directory);
            logger.info("Répertoire supprimé avec succès : {}", directoryPath);
        } catch (IOException e) {
            String errorMsg = "Échec de la suppression du répertoire " + directoryPath;
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    @Override
    public byte[] readFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            return Files.readAllBytes(file);
        } catch (IOException e) {
            String errorMsg = "Échec de la lecture du fichier " + filename;
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    public Path getRootLocation() {
        return rootLocation;
    }
} 