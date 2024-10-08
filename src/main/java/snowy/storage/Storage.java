package snowy.storage;

import snowy.tasklist.Task;
import snowy.storage.TaskDecoder;
import snowy.data.SnowyException;
import snowy.tasklist.TaskList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;

public class Storage {

    private final Path taskFilePath;
    private final TaskDecoder taskDecoder;

    /**
     * Constructs a Storage object and initializes the file and directory structure.
     * If the directory or file does not exist, they are created. If the file exists,
     * it is cleared upon initialization.
     *
     * @param directoryPath the path to the directory where the task file will be stored
     * @param fileName the name of the task file
     */
    public Storage(String directoryPath, String fileName) {
        Path dataDirectoryPath = Paths.get(directoryPath);
        this.taskFilePath = dataDirectoryPath.resolve(fileName);
        this.taskDecoder = new TaskDecoder();

        try {
            if (Files.notExists(dataDirectoryPath)) {
                Files.createDirectories(dataDirectoryPath);
            }
            if (Files.notExists(taskFilePath)) {
                Files.createFile(taskFilePath);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while setting up storage: " + e.getMessage());
        }
    }

    /**
     * Clears the content of the task file by writing an empty string to it.
     *
     * @throws IOException if an I/O error occurs while clearing the file
     */
    private void clearFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(taskFilePath.toString(), false))) {
            writer.write("");
        } catch (IOException e) {
            throw new IOException("Failed to clear file: ", e);
        }
    }

    /**
     * Writes a task to the file. The task is appended to the end of the file.
     *
     * @param task the task to be written to the file
     */
    public void writeTaskToFile(Task task) throws SnowyException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(taskFilePath.toString(), true))) {
            writer.write(task.toString());
            writer.newLine();
        } catch (IOException e) {
            throw new SnowyException("Error while writing to file: " + e.getMessage());
        }
    }

    /**
     * Deletes a task from the file by copying all tasks except the one to be deleted
     * to a temporary file, then replacing the original file with the temporary file.
     *
     * @param task the task to be deleted from the file
     */
    public void deleteTaskFromFile(Task task) {
        Path tempFilePath = taskFilePath.getParent().resolve("temp.txt");
        String search = task.toString();

        try (BufferedReader br = new BufferedReader(new FileReader(taskFilePath.toString()));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFilePath.toString()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.equals(search)) {
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while deleting the task from the file: " + e.getMessage());
            return;
        }

        try {
            Files.delete(taskFilePath);
            Files.move(tempFilePath, taskFilePath);
            System.out.println("Task deleted from file successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while replacing the file: " + e.getMessage());
        }
    }

    public void editTask(String before, String after) {
        Path tempFilePath = taskFilePath.getParent().resolve("temp.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(taskFilePath.toString()));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFilePath.toString()))) {
            String line;
            while ((line = br.readLine()) != null) {
               if (line.equals(before)) {
                    bw.write(after);
                } else {
                    bw.write(line);
                }
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("An error occurred while editing the task in the file: " + e.getMessage());
            return;
        }

        try {
            Files.delete(taskFilePath);  // Delete the original file
            Files.move(tempFilePath, taskFilePath);  // Move the temp file to the original file location
            System.out.println("Task edited successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while replacing the file: " + e.getMessage());
        }
    }


    /**
     * Loads tasks from the file into a list of strings.
     * Each line in the file corresponds to a task.
     *
     */
    public void loadTasksFromFile(ArrayList<Task> taskList) throws SnowyException {
        try (BufferedReader reader = new BufferedReader(new FileReader(taskFilePath.toString()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    Task task = taskDecoder.decode(line);
                    taskList.add(task);
                } catch (SnowyException e) {
                    System.out.println("Error decoding task: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading from the file: " + e.getMessage());
        }
    }
}
