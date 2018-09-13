package technology.dice.dicewhere.testutils;

import com.google.common.collect.ImmutableList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileLineSubsetBuilder {
  private final List<Path> paths;
  private final int linesFromEach;

  public FileLineSubsetBuilder(int linesFromEach, Path... paths) {
    this(linesFromEach, Arrays.stream(paths).collect(Collectors.toList()));
  }

  public FileLineSubsetBuilder(int linesFromEach, List<Path> paths) {
    this.linesFromEach = linesFromEach;
    this.paths = ImmutableList.copyOf(paths);
  }

  public void writeTo(Path target) throws IOException {
    for (Path path : paths) {
      long nrLines = Files.lines(path).count();
      long outputEvery = (nrLines / linesFromEach);
      BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
      PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(target.toFile())));
      String line;
      long lineNumber = 0;
      while ((line = reader.readLine()) != null) {
        if (lineNumber == outputEvery) {
          writer.println(line);
          lineNumber = 0;
        }
        lineNumber++;
      }
      reader.close();
      writer.flush();
      writer.close();
    }
  }

  public static void main(String[] args) throws IOException {
    int linesFromEach = Integer.parseInt(args[0]);
    Path dest = Paths.get(args[1]);
    List<Path> sources =
        Arrays.stream(args).skip(2).map(p -> Paths.get(p)).collect(Collectors.toList());
    FileLineSubsetBuilder fileLineSubsetBuilder = new FileLineSubsetBuilder(linesFromEach, sources);
    fileLineSubsetBuilder.writeTo(dest);
  }
}
