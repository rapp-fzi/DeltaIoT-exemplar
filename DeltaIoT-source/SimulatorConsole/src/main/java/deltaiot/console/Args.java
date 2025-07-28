package deltaiot.console;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.EnumConverter;
import com.beust.jcommander.converters.PathConverter;

import mapek.strategy.AdaptionStrategyFactory.Kind;

public class Args {
    @Parameter(names = { "-h", "--help" }, help = true)
    public boolean help;

    @Parameters
    public static class CommandStrategy {

        public static class KindConverter extends EnumConverter<Kind> {

            public KindConverter(String optionName) {
                super(optionName, Kind.class);
            }
        }

        public static class PathExistsValidator implements IParameterValidator {

            @Override
            public void validate(String name, String value) throws ParameterException {
                Path path = Paths.get(value);
                if (!Files.exists(path)) {
                    throw new ParameterException(String.format("Parameter %s: file not exists: %s", name, value));
                }
            }
        }

        @Parameter(names = { "-a", "--adaption" }, required = true, converter = KindConverter.class)
        public Kind strategyKind = null;

        @Parameter(names = { "-p",
                "--param" }, required = true, description = "json parameter file", converter = PathConverter.class, validateWith = PathExistsValidator.class)
        public Path parameterFile;
    }
}
