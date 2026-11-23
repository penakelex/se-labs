package org.nstu.se.lab4.ui.dialog;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.nstu.se.lab4.Messages;
import org.nstu.se.lab4.audio.source.WaveSource;

import java.util.Optional;

public final class SourceFileChooser {
    private static final String KEY_WAV_FILTER = "file.chooser.wav";
    private static final String KEY_ERROR_FILE = "error.file";
    private static final String WAV_EXTENSION = "*.wav";

    public record PickedSource(WaveSource source, String name) {}

    private SourceFileChooser() {}

    public static Optional<PickedSource> pickAndLoad(final Window owner) {
        final var chooser = new FileChooser();
        chooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(Messages.get(KEY_WAV_FILTER), WAV_EXTENSION));

        final var file = chooser.showOpenDialog(owner);

        if (file == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(
                    new PickedSource(WaveSource.fromFile(file.toPath()), file.getName()));
        } catch (final IllegalArgumentException exception) {
            final var alert =
                    new Alert(
                            Alert.AlertType.ERROR, Messages.format(KEY_ERROR_FILE, file.getName()));
            alert.initOwner(owner);
            alert.setHeaderText(null);
            alert.showAndWait();
            return Optional.empty();
        }
    }
}
