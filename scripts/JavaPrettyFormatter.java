import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.Pretty;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public final class JavaPrettyFormatter {
    private JavaPrettyFormatter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("Expected at least one Java source path.");
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("JDK compiler is not available.");
        }

        List<Path> sourcePaths = new ArrayList<>();
        for (String arg : args) {
            sourcePaths.add(Path.of(arg));
        }

        try (StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> fileObjects =
                    fileManager.getJavaFileObjectsFromPaths(sourcePaths);
            JavacTask task = (JavacTask)
                    compiler.getTask(
                            null,
                            fileManager,
                            null,
                            List.of("--release", "25"),
                            null,
                            fileObjects);

            Iterable<? extends CompilationUnitTree> parsedUnits = task.parse();
            for (CompilationUnitTree parsedUnit : parsedUnits) {
                JCCompilationUnit compilationUnit = (JCCompilationUnit) parsedUnit;
                StringWriter writer = new StringWriter();
                Pretty pretty = new Pretty(writer, true);
                pretty.printUnit(compilationUnit, compilationUnit.sourcefile);
                String formatted = normalizeLineEndings(writer.toString()).trim() + System.lineSeparator();
                Path sourcePath = Path.of(compilationUnit.getSourceFile().toUri());
                Files.writeString(sourcePath, formatted, StandardCharsets.UTF_8);
            }
        }
    }

    private static String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n').replace("\n", System.lineSeparator());
    }
}
