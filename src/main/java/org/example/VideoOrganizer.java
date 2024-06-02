package org.example;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.mov.media.QuickTimeVideoDirectory;
import com.drew.metadata.mp4.Mp4Directory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;


//This filter out all the videos from any folder recursivly
public class VideoOrganizer {

    public static void main(String[] args) {
        String sourceDirectory = "/Users/deepakvishwakarma/deepak-drive-e/working20-24";
        String videoDestination = sourceDirectory + "_videos";  // Replace with the path to your destination video directory
        try {
            organizeVideos(sourceDirectory, videoDestination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void organizeVideos(String sourceDirectory, String videoDestination) throws IOException {
        Files.walkFileTree(Paths.get(sourceDirectory), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!file.toFile().getName().endsWith(".DS_Store")) {
                    if (isVideo(file)) {
                        moveFile(file, videoDestination);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void moveFile(Path file, String videoDestination) throws IOException {
        Path destinationDirectory = Paths.get(videoDestination);

        // Create the destination directory if it doesn't exist
        if (Files.notExists(destinationDirectory)) {
            Files.createDirectories(destinationDirectory);
        }

        // Move the file to the destination directory
        Files.move(file, Paths.get(destinationDirectory.toString(), file.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
    }

    private static boolean isVideo(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return fileName.endsWith(".mov") || fileName.endsWith(".mp4") || fileName.endsWith(".avi")
                || fileName.endsWith(".mkv") || fileName.endsWith(".flv") || fileName.endsWith(".wmv");
    }

    private static Date getCreationDateForVideo(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            QuickTimeVideoDirectory videoDirectory = metadata.getFirstDirectoryOfType(QuickTimeVideoDirectory.class);

            if (videoDirectory != null && videoDirectory.containsTag(QuickTimeVideoDirectory.TAG_CREATION_TIME)) {
                return videoDirectory.getDate(QuickTimeVideoDirectory.TAG_CREATION_TIME);
            }

            Mp4Directory mp4Directory = metadata.getFirstDirectoryOfType(com.drew.metadata.mp4.Mp4Directory.class);

            if (mp4Directory != null && mp4Directory.containsTag(Mp4Directory.TAG_CREATION_TIME)) {
                return mp4Directory.getDate(Mp4Directory.TAG_CREATION_TIME);
            }

        } catch (IOException | ImageProcessingException e) {
            e.printStackTrace();
            return new Date(file.lastModified());
        }
        return null;
    }
}
