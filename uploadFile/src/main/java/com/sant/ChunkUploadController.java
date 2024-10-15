/**
 * @author santji
 * @date 15-Oct-2024
 */
package com.sant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin("*")
public class ChunkUploadController {

    private static final String CHUNKS_DIR = "/home/santji/Documents/Java_Project/uploadFile/chunks";  // Directory to save chunks temporarily

    @PostMapping("/upload-chunk")
    public ResponseEntity<String> uploadChunk(@RequestParam("file") MultipartFile chunk,
                                              @RequestParam("fileName") String fileName,
                                              @RequestParam("chunkIndex") int chunkIndex,
                                              @RequestParam("totalChunks") int totalChunks) throws IOException {

        // Save the chunk to the server
        File chunkDir = new File(CHUNKS_DIR, fileName);
        if (!chunkDir.exists()) {
            chunkDir.mkdirs();  // Create directory for the file chunks if it doesn't exist
        }

        File chunkFile = new File(chunkDir, fileName + ".part" + chunkIndex);
        chunk.transferTo(chunkFile);  // Save the chunk to disk

        return ResponseEntity.ok("Chunk " + chunkIndex + " uploaded successfully");
    }
    
    @PostMapping("/merge-chunks")
    public ResponseEntity<String> mergeChunks(@RequestBody Map<String, String> requestBody) throws IOException {
        String fileName = requestBody.get("fileName");
        File chunkDir = new File("/home/santji/Documents/Java_Project/uploadFile/chunks", fileName);

        // Define the merged file path
        File mergedFile = new File("/home/santji/Documents/Java_Project/uploadFile/mergedFile", fileName);

        // Ensure the parent directory for the merged file exists
        File mergedFileDir = mergedFile.getParentFile();
        if (!mergedFileDir.exists()) {
            mergedFileDir.mkdirs();  // Create the directory if it does not exist
        }

        // Check if a directory with the same name as the file already exists
        if (mergedFile.exists() && mergedFile.isDirectory()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("A directory exists with the same name as the target file!");
        }

        // Create a new merged file
        try (FileOutputStream fos = new FileOutputStream(mergedFile)) {
            int totalChunks = chunkDir.listFiles().length;
            
            // Loop through and append all chunk files
            for (int i = 0; i < totalChunks; i++) {
                File chunkFile = new File(chunkDir, fileName + ".part" + i);
                Files.copy(chunkFile.toPath(), fos);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error merging file: " + e.getMessage());
        }

        // Clean up chunk files after successful merging
        for (File chunkFile : chunkDir.listFiles()) {
            chunkFile.delete();
        }
        chunkDir.delete();

        return ResponseEntity.ok("File merged successfully");
    }

}

