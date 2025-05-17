package com.filesmanager.app.controllers;

import com.filesmanager.domain.ports.FileStoragePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Files API", description = "API pour la gestion des fichiers")
public class FileController {

    private final FileStoragePort fileStoragePort;

    @Operation(summary = "Téléverser un fichier",
            description = "Permet de téléverser un fichier sur le serveur. Utilisez targetPath avec '_' comme séparateur pour spécifier le répertoire cible (ex: '2025_Janvier_Factures')")
    @ApiResponse(responseCode = "200", description = "Fichier téléversé avec succès")
    @ApiResponse(responseCode = "400", description = "Erreur lors du téléversement")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @Parameter(description = "Fichier à téléverser")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Chemin cible (optionnel). Utilisez '_' comme séparateur. Example: '2025_Janvier_Factures'")
            @RequestParam(value = "targetPath", required = false, defaultValue = "") String targetPath) {
        try {
            String normalizedPath = targetPath.isEmpty() ? file.getOriginalFilename() :
                    targetPath.replace('_', '/') + "/" + file.getOriginalFilename();
            fileStoragePort.storeFile(normalizedPath, file.getBytes());
            return ResponseEntity.ok("File uploaded successfully: " + normalizedPath);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Could not upload file: " + e.getMessage());
        }
    }

    @Operation(summary = "Lister les fichiers",
            description = "Récupère la liste de tous les fichiers stockés")
    @ApiResponse(responseCode = "200", description = "Liste des fichiers récupérée avec succès")
    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles() {
        List<String> files = fileStoragePort.listFiles().stream()
                .map(Path::toString)
                .collect(Collectors.toList());
        return ResponseEntity.ok(files);
    }

    @Operation(summary = "Supprimer un fichier",
            description = "Supprime un fichier spécifique")
    @ApiResponse(responseCode = "200", description = "Fichier supprimé avec succès")
    @ApiResponse(responseCode = "400", description = "Erreur lors de la suppression")
    @DeleteMapping("/{filename}")
    public ResponseEntity<String> deleteFile(
            @Parameter(description = "Nom du fichier à supprimer")
            @PathVariable String filename) {
        try {
            fileStoragePort.deleteFile(filename);
            return ResponseEntity.ok("File deleted successfully: " + filename);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Could not delete file: " + e.getMessage());
        }
    }

    @Operation(summary = "Supprimer un répertoire",
            description = "Supprime un répertoire et tout son contenu")
    @ApiResponse(responseCode = "200", description = "Répertoire supprimé avec succès")
    @ApiResponse(responseCode = "400", description = "Erreur lors de la suppression")
    @DeleteMapping("/directory/{directoryPath}")
    public ResponseEntity<String> deleteDirectory(
            @Parameter(description = "Chemin du répertoire à supprimer")
            @PathVariable String directoryPath) {
        try {
            fileStoragePort.deleteDirectory(directoryPath);
            return ResponseEntity.ok("Directory deleted successfully: " + directoryPath);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Could not delete directory: " + e.getMessage());
        }
    }

    @Operation(summary = "Créer un nouveau répertoire",
            description = "Crée un nouveau répertoire dans le système de fichiers. Utilisez '_' pour représenter les sous-répertoires. Example: '2025_Janvier' créera Janvier dans 2025")
    @ApiResponse(responseCode = "200", description = "Répertoire créé avec succès")
    @ApiResponse(responseCode = "400", description = "Erreur lors de la création")
    @PostMapping("/directory/{directoryPath}")
    public ResponseEntity<String> createDirectory(
            @Parameter(description = "Chemin du répertoire à créer. Utilisez '_' pour les sous-répertoires. Example: '2025_Janvier'")
            @PathVariable String directoryPath) {
        try {
            // Remplacer le caractère '_' par le séparateur de fichier du système
            String normalizedPath = directoryPath.replace('_', '/');
            fileStoragePort.createDirectory(normalizedPath);
            return ResponseEntity.ok("Directory created successfully: " + normalizedPath);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Could not create directory: " + e.getMessage());
        }
    }
} 