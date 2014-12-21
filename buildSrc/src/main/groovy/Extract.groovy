import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import tools.Extractor

class ExtractTask extends DefaultTask
{

  @InputDirectory
  def File inputDir

  @OutputDirectory
  def File outputDir


  @TaskAction
  def extract()
  {
    Extractor extractor = new Extractor( inputDir.getAbsolutePath(), outputDir );
    extractor.extractCode( inputDir );
  }
}