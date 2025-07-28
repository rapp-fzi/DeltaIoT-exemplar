package deltaiot.console;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.EnumConverter;

import mapek.strategy.AdaptionStrategyFactory.Kind;

public class Args {
    public static class KindConverter extends EnumConverter<Kind> {

        public KindConverter(String optionName) {
            super(optionName, Kind.class);
        }
    }

    @Parameter(names = { "-h", "--help" }, help = true)
    public boolean help;

    @Parameter(names = { "-a", "--adaption" }, converter = KindConverter.class)
    public Kind strategyKind = null;
}
