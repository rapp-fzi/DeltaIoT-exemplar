package mapek;

import java.io.IOException;

public interface IAdaptionStrategy {
    String getId();

    void start() throws IOException;

}
