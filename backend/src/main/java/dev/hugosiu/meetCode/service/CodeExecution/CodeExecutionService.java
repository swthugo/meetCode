package dev.hugosiu.meetCode.service.CodeExecution;

import dev.hugosiu.meetCode.constant.CodeExecuteConstant;
import dev.hugosiu.meetCode.dto.CompilerConsoleDTO;
import dev.hugosiu.meetCode.dto.RunConsoleDTO;
import dev.hugosiu.meetCode.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CodeExecutionService {


  @Value("${run.test.path}")
  private String runTestPath;

  static String JUNIT_STANDALONE_JAR_TEMP = "/config/junit-platform-console-standalone-1.5.2.jar";
//  static String runTestPath = "/config/bin";

  @Value("file:/config/junit-platform-console-standalone-1.5.2.jar")
  Resource junitStandalone;

  private static void deleteJavaFile(Path path) throws IOException {
    if (Files.exists(path)) {
      Files.delete(path);
      System.out.println("File deleted: " + path);
    }
  }

  private static CompilerConsoleDTO compiler(List<String> filePaths) throws IOException, ResourceNotFoundException {
    if (!Files.exists(Paths.get(JUNIT_STANDALONE_JAR_TEMP))) {
      System.err.println("Files doesn't exist: " + JUNIT_STANDALONE_JAR_TEMP);
      throw new ResourceNotFoundException("Standalone Jar Not Found: " + JUNIT_STANDALONE_JAR_TEMP);
    }

    for (String filePath : filePaths) {
      File file = new File(filePath);
      if (!file.exists()) {
        System.out.println("File does not exist: " + filePath);
        throw new ResourceNotFoundException("File Not Found: " + filePath);
      }
    }

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    if (compiler == null) {
      System.err.println("Java compiler is not available.");
      throw new ResourceNotFoundException("Java compiler Not Found!");
    }

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8);
    Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(filePaths.stream().map(File::new).collect(Collectors.toList()));
    JavaCompiler.CompilationTask task = compiler.getTask(
            null,
            fileManager,
            diagnostics,
            Arrays.asList("-cp", JUNIT_STANDALONE_JAR_TEMP),
            null,
            compilationUnits);

//    javac -cp /config/junit-platform-console-standalone-1.5.2.jar:/config/bin/classes /config/bin/test-classes/user_1/problem_1/ProblemTest.java

    boolean success = task.call();
    fileManager.close();

    StringBuilder result = new StringBuilder("error:");

    if (!success) {
      for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
        result.append(diagnostic.toString().split(": error:")[1]);
      }
    }

    System.out.println(result);
    return CompilerConsoleDTO.builder()
            .isValid(success)
            .message(result.toString())
            .build();
  }

  private static FileEditor writeCodeFile(TestEnv testEnv, String code) {
    String codePath = testEnv.getClasses() + "/" + CodeExecuteConstant.CODE_CLASS_PREFIX
            + CodeExecuteConstant.FILE_TYPE;
    String content = testEnv.getPackageName() + ";\n\n" + CodeExecuteConstant.MAIN_CLASS_SAMPLE + "\n" + code;
    return new FileEditor(codePath, content);
  }

  private static FileEditor writeCodeTestFile(TestEnv testEnv, String code) {
    String codePath = testEnv.getTestClasses() + "/" + CodeExecuteConstant.CODE_CLASS_PREFIX
            + CodeExecuteConstant.TEST_CLASS_SUFFIX + CodeExecuteConstant.FILE_TYPE;
    String content = testEnv.getPackageName() + ";\n\n" + code;
    return new FileEditor(codePath, content);
  }

  private static void writeJavaFile(String filePath, String code) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
      Files.writeString(Paths.get(filePath), code, StandardCharsets.UTF_8,
              StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      writer.write(code);
    } catch (Exception e) {
      System.err.println("Failed to write to file: " + e.getMessage());
    }
  }

  private static void createJavaFile(FileEditor fileEditor) throws IOException {
    createFile(Paths.get(fileEditor.getPath()));
    writeJavaFile(fileEditor.getPath(), fileEditor.getMessage());
  }

  private static void createFile(Path path) throws IOException {
    if (!Files.exists(path)) {
      Files.createFile(path);
      System.out.println("File created: " + path);
    } else {
      System.err.println("File already existed: {" + path + "}");
    }
  }

  private static void createDirectory(Path path) throws IOException {
    if (!Files.exists(path)) {
      Files.createDirectory(path);
      System.out.println("Directory created: " + path);
    } else {
      System.err.println("Directory already existed: {" + path + "}");
    }
  }

  public RunConsoleDTO executeCode(Long userId, Long problemId, String code, String testCode) throws IOException, ResourceNotFoundException {
    TestEnv testEnv = createDirectories(userId, problemId);

    FileEditor file = writeCodeFile(testEnv, code);
    FileEditor testFile = writeCodeTestFile(testEnv, testCode);
    createJavaFile(file);
    createJavaFile(testFile);

    List<String> filePaths = new ArrayList<>() {{
      add(file.getPath());
      add(testFile.getPath());
    }};

    CompilerConsoleDTO compilerConsole = compiler(filePaths);

    if (!compilerConsole.isValid()) {
      return RunConsoleDTO.builder()
              .success(compilerConsole.isValid())
              .message(compilerConsole.getMessage())
              .build();
    }

    return runTestJar(userId, problemId);
  }

  private RunConsoleDTO runTestJar(Long userId, Long problemIdx) throws IOException {

    String classPath = runTestPath + "/classes";
    String testClassPath = runTestPath + "/test-classes";
    String targetPackage = "user_" + userId + ".problem_" + problemIdx;

    // /config/bin/user_1/problem_1/classes:/config/bin/user_1/problem_1/test-classes

//    String logFilePath = classPath + "/" + CodeExecuteConstant.LOG_FILE_NAME;
    String logFilePath = runTestPath + "/" + targetPackage + "-log.txt";
    String fullPath = classPath + ":" + testClassPath;

    int exitCode = Integer.MIN_VALUE;

    System.out.println("!!!!!!!   HERE JAVA COMMAND");
    System.out.println("java -jar " + JUNIT_STANDALONE_JAR_TEMP + " --select-package " + targetPackage + " --cp " + fullPath);

    ProcessBuilder pb = new ProcessBuilder("java",
            "-jar", JUNIT_STANDALONE_JAR_TEMP,
            "--select-package", targetPackage,
            "--cp", fullPath);


//    java -jar /config/junit-platform-console-standalone-1.5.2.jar --select-package user_1.problem_1 --cp /config/bin/classes:/config/bin/test-classes
    pb.redirectOutput(new File(logFilePath));

    try {
      Process process = pb.start();
      exitCode = process.waitFor();
      System.out.println(
              exitCode == 0
                      ? "Run tests successful."
                      : "Run tests failed. Exit code: " + exitCode
      );
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    StringBuilder output = new StringBuilder();
    BufferedReader reader;

    try {
      reader = new BufferedReader(new FileReader(logFilePath));
      String line = reader.readLine();
      line = reader.readLine();
      line = reader.readLine();
      line = reader.readLine();

      while (line != null) {
        output.append(line).append("\n");
        line = reader.readLine();
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return RunConsoleDTO.builder()
            .success(exitCode == 0)
            .message(output.toString())
            .build();
  }

  private TestEnv createDirectories(Long userId, Long problemId) throws IOException {
    String[] directories = {
            runTestPath,
            runTestPath + "/classes",
            runTestPath + "/classes/user_" + userId,
            runTestPath + "/classes/user_" + userId + "/problem_" + problemId,
            runTestPath + "/test-classes/",
            runTestPath + "/test-classes/user_" + userId,
            runTestPath + "/test-classes/user_" + userId + "/problem_" + problemId,
    };

    for (String directory : directories) {
      createDirectory(Paths.get(directory));
    }

    return new TestEnv(
            runTestPath + "/classes/user_" + userId + "/problem_" + problemId,
            runTestPath + "/test-classes/user_" + userId + "/problem_" + problemId,
            CodeExecuteConstant.PACKAGE_NAME_HEAD + userId + CodeExecuteConstant.PACKAGE_NAME_TAIL + problemId
    );
  }
}