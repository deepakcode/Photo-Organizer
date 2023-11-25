package org.example;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.media.QuickTimeVideoDirectory;
import com.drew.metadata.mp4.Mp4Directory;

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
                        moveFile(file, creationDate, destinationRoot);
                    }
                }
                if (isQuickTimeVideo(file)) {
                    Date creationDate = getCreationDateForVideo(file.toFile());
                    if (creationDate != null) {
                        moveFile(file, creationDate, destinationRoot);
//                        String monthYearFolderName = new SimpleDateFormat("yyyy-MM").format(creationDate);
//                        Path destinationDirectory = Paths.get(destinationRoot, monthYearFolderName);
//
//                        // Create the destination directory if it doesn't exist
//                        if (Files.notExists(destinationDirectory)) {
//                            Files.createDirectories(destinationDirectory);
//                        }
//
//                        // Move the file to the destination directory
//                        Files.move(file, Paths.get(destinationDirectory.toString(), file.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void moveFile(Path file, Date creationDate, String destinationRoot) throws IOException {
        String monthFolderName = new SimpleDateFormat("yyyy-MM").format(creationDate);
        Path destinationDirectory = Paths.get(destinationRoot, monthFolderName);

        // Create the destination directory if it doesn't exist
        if (Files.notExists(destinationDirectory)) {
            Files.createDirectories(destinationDirectory);
        }

        // Move the file to the destination directory
        Files.move(file, Paths.get(destinationDirectory.toString(), file.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
    }

    private static boolean isPhoto(Path file) {
        return true;
//        String fileName = file.getFileName().toString().toLowerCase();
//        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png");
    }

    private static boolean isQuickTimeVideo(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return fileName.endsWith(".mov") || fileName.endsWith(".mp4");
    }

    private static Date getCreationDate(File file) {

        if (file.isFile() && file.getName().toLowerCase().endsWith(".aae")) {
            System.out.println("Deleting .AAE file: " + file.getName());
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("File deleted successfully. " + file);
            } else {
                System.out.println("Failed to delete file. " + file);
            }
            return null;
        }

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



        return null;
    }

    private static Date getCreationDateForVideo(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);


            QuickTimeVideoDirectory videoDirectory = metadata.getFirstDirectoryOfType(QuickTimeVideoDirectory.class);

            if (videoDirectory != null && videoDirectory.containsTag(QuickTimeVideoDirectory.TAG_CREATION_TIME)) {
                return videoDirectory.getDate(QuickTimeVideoDirectory.TAG_CREATION_TIME);
            }

            Mp4Directory mp4Directory =  metadata.getFirstDirectoryOfType(com.drew.metadata.mp4.Mp4Directory.class);

            if (mp4Directory != null && mp4Directory.containsTag(Mp4Directory.TAG_CREATION_TIME)) {
                return mp4Directory.getDate(Mp4Directory.TAG_CREATION_TIME);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ImageProcessingException e) {
            System.out.println("ImageProcessingException so organizing based upon  last modified date : "+file.lastModified()+"for file : "+file );
            System.out.println(e.getMessage());
            return new Date(file.lastModified());
            //throw new RuntimeException(e);
        }

        return null;
    }
}