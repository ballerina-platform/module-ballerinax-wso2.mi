package io.ballerina.stdlib.mi.plugin;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import io.ballerina.stdlib.mi.plugin.model.ModelElement;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {

    /** 
       These are private utility functions used in the generateXml method
     */
    private static String readXml(String fileName) throws IOException {
        InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream(fileName);
        assert inputStream != null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder xmlContent = new StringBuilder();
        int character;
        while ((character = reader.read()) != -1) {
            xmlContent.append((char) character);
        }
        reader.close();
        return xmlContent.toString();
    }

    /** 
       These are private utility functions used in the generateXml method
     */
    private static void writeXml(String fileName, String content) throws IOException {
        FileWriter myWriter = new FileWriter(fileName);
        myWriter.write(content);
        myWriter.close();
    }

    /**
     * Generate XML file using the provided template and model element.
     *
     * @param templateName Name of the template file
     * @param outputName Name of the output file
     * @param element Model element(connector/component) to be used in the template
     * @Note: This method generates the XMLs that is needed for the connector, which uses the ReadXml and WriteXml methods.
     */
    public static void generateXml(String templateName, String outputName, ModelElement element){
        try {
            Handlebars handlebar = new Handlebars();
            String templateFileName = String.format("%s.xml", templateName);
            String xmlContent = readXml(templateFileName);
            Template template = handlebar.compileInline(xmlContent);
            String output = template.apply(element);

            String outputFileName = String.format("%s.xml", outputName);
            writeXml(outputFileName, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Zip a folder and its contents.
     *
     * @param sourceDirPath Path to the source directory
     * @param zipFilePath Path to the output ZIP file
     * @throws IOException If an I/O error occurs
     * @Note : This method is used to zip the Constants.CONNECTOR directory and create a zip file using the module name and Constants.ZIP_FILE_SUFFIX
     */
    public static void zipFolder(Path sourceDirPath, String zipFilePath) throws IOException {
        Path sourceDir = sourceDirPath;
        try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(Paths.get(zipFilePath)))) {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path targetFile = sourceDir.relativize(file);
                    outputStream.putNextEntry(new ZipEntry(targetFile.toString()));

                    Files.copy(file, outputStream);
                    outputStream.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!dir.equals(sourceDir)) {
                        Path targetDir = sourceDir.relativize(dir);
                        outputStream.putNextEntry(new ZipEntry(targetDir.toString() + "/"));
                        outputStream.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /**
     * Delete a directory and all its contents.
     *
     * @param dirPath Path to the directory to be deleted
     * @throws IOException If an I/O error occurs
     *
     *
     * @Note : This method is used to delete the intermediate Constants.CONNECTOR directory
     */

    public static void deleteDirectory(Path dirPath) throws IOException {
        Path directory = dirPath;
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Move resources from the resources folder to the destination folder.
     *
     * @param classLoader Class loader to load the resources
     * @param destination Path to the destination folder
     * @throws IOException If an I/O error occurs
     * @throws URISyntaxException If an URI syntax error occurs
     *
     * @Note : This method is used to move the resources from the resources folder to the Constants.CONNECTOR directory
     */
    public static void moveResources(ClassLoader classLoader, Path destination) throws IOException, URISyntaxException {
        String input = "mediator-classes";
        String resourcePath = classLoader.getResource(input).getPath();
        String replacedString = resourcePath.replace("!/connector-new", "");
        URI uri = URI.create("jar:"+replacedString);
        List<Path> paths = new ArrayList<>();
        FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
        try {
            // TODO: Change this to walk once
            paths = Files.walk(fs.getPath("mediator-classes"))
                    .filter(f -> f.toString().contains(".class") || f.toString().contains(".jar") ||f.toString().contains(".png"))
                    .toList();

            Path zipOutPath = destination;
            Files.createDirectories(zipOutPath); // Create destination directory if it doesn't exist

            for (Path path : paths) {
                Path relativePath = fs.getPath(input).relativize(path);
                Path outputPath = zipOutPath.resolve(relativePath.toString());
                Files.createDirectories(outputPath.getParent()); // Create parent directories if they don't exist
                InputStream inputStream = getFileFromResourceAsStream(classLoader, path.toString());
                Files.copy(inputStream, outputPath);
            }

            paths = Files.walk(fs.getPath("icon"))
                    .filter(f -> f.toString().contains(".png"))
                    .toList();
            for (Path path : paths) {
                Path outputPath = zipOutPath.resolve(path.toString());
                Files.createDirectories(outputPath.getParent());
                InputStream inputStream = getFileFromResourceAsStream(classLoader, path.toString());
                Files.copy(inputStream, outputPath);
            }

            paths = Files.walk(fs.getPath("lib"))
                    .filter(f -> f.toString().contains(".jar"))
                    .toList();
            for (Path path : paths) {
                Path outputPath = zipOutPath.resolve(path.toString());
                Files.createDirectories(outputPath.getParent());
                InputStream inputStream = getFileFromResourceAsStream(classLoader, path.toString());
                Files.copy(inputStream, outputPath);
            }
        } finally {
            fs.close(); // Close the FileSystem
        }
    }

    /**
     * These are private utility functions used in the moveResources method
     */
    private static InputStream getFileFromResourceAsStream(ClassLoader classLoader, String fileName) {
        // The class loader that loaded the class
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }
    }
}
