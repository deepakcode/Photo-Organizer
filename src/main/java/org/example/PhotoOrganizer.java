package org.example;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoOrganizer {

    public static void main(String[] args) {
        String sourceDirectory = "/Users/deepakvishwakarma/deepak-drive-f/iCloudPhotosLivePhotos";  // Replace with the path to your source directory
        String destinationRoot = "/Users/deepakvishwakarma/deepak-drive-f/output-images";  // Replace with the path to your destination root directory

        try {
            organizePhotos(sourceDirectory, destinationRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void organizePhotos(String sourceDirectory, String destinationRoot) throws IOException {
        Files.walkFileTree(Paths.get(sourceDirectory), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (isPhoto(file)) {
                    Date creationDate = getCreationDate(file.toFile());
                    if (creationDate != null) {
                        String monthFolderName = new SimpleDateFormat("yyyy-MM").format(creationDate);
                        Path destinationDirectory = Paths.get(destinationRoot, monthFolderName);

                        // Create the destination directory if it doesn't exist
                        if (Files.notExists(destinationDirectory)) {
                            Files.createDirectories(destinationDirectory);
                        }

                        // Move the file to the destination directory
                        Files.move(file, Paths.get(destinationDirectory.toString(), file.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static boolean isPhoto(Path file) {
        return true;
//        String fileName = file.getFileName().toString().toLowerCase();
//        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png");
    }

    private static Date getCreationDate(File file) {
        try {

            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory != null && directory.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) {
                Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                return date;
            }

            return new Date(file.lastModified());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ImageProcessingException e) {
            System.out.println("ImageProcessingException : "+file);
            //throw new RuntimeException(e);
        }

         if (file.isFile() && file.getName().toLowerCase().endsWith(".aae")) {
             System.out.println("Deleting .AAE file: " + file.getName());
             boolean deleted = file.delete();
             if (deleted) {
                 System.out.println("File deleted successfully. " + file);
             } else {
                 System.out.println("Failed to delete file. " + file);
             }
         }

        return null;
    }
}